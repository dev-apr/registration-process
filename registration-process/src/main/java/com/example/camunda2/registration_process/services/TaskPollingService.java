package com.example.camunda2.registration_process.services;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service that polls for newly created tasks and assigns them automatically
 */
@Service
@EnableScheduling
public class TaskPollingService {

    private static final Logger LOGGER = Logger.getLogger(TaskPollingService.class.getName());
    
    private final CamundaTaskListClient taskListClient;
    private final DbTaskAssignmentService taskAssignmentService;
    private final RoundRobinAssignmentService roundRobinService;
    private final ProcessVariableService processVariableService;
    private final ZeebeClient zeebeClient;
    
    private JobWorker committeeAssignmentWorker;
    
    // Keep track of tasks we've already processed to avoid duplicates
    private final Set<String> processedTaskIds = new HashSet<>();
    
    @Autowired
    public TaskPollingService(
            CamundaTaskListClient taskListClient,
            DbTaskAssignmentService taskAssignmentService,
            RoundRobinAssignmentService roundRobinService,
            ProcessVariableService processVariableService,
            ZeebeClient zeebeClient) {
        this.taskListClient = taskListClient;
        this.taskAssignmentService = taskAssignmentService;
        this.roundRobinService = roundRobinService;
        this.processVariableService = processVariableService;
        this.zeebeClient = zeebeClient;
        LOGGER.info("TaskPollingService initialized");
    }
    
    @jakarta.annotation.PostConstruct
    public void startWorkers() {
        // Start a worker for the "assign-to-violation-committee" service task
        committeeAssignmentWorker = zeebeClient.newWorker()
            .jobType("assign-to-violation-committee")
            .handler(new CommitteeAssignmentHandler())
            .timeout(Duration.ofMinutes(5))
            .open();
        
        LOGGER.info("Committee assignment worker started");
    }
    
    @jakarta.annotation.PreDestroy
    public void stopWorkers() {
        if (committeeAssignmentWorker != null) {
            committeeAssignmentWorker.close();
            LOGGER.info("Committee assignment worker stopped");
        }
    }
    
    /**
     * Handler for the "assign-to-violation-committee" service task
     */
    private class CommitteeAssignmentHandler implements JobHandler {
        @Override
        public void handle(JobClient client, ActivatedJob job) {
            try {
                LOGGER.info("Handling committee assignment job: " + job.getKey());
                
                // Get the process instance ID
                String processInstanceId = String.valueOf(job.getProcessInstanceKey());
                
                // Select the next committee (c1 or c2) in round-robin fashion
                String committee = roundRobinService.selectNextCommittee();
                LOGGER.info("Selected committee: " + committee + " for process: " + processInstanceId);
                
                // Store the selected committee for this process instance
                roundRobinService.storeCommitteeForProcess(processInstanceId, committee);
                
                // Create variables to pass back to the process
                Map<String, Object> variables = new HashMap<>();
                variables.put("selectedCommittee", committee);
                
                // Complete the job with the variables
                client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
                
                LOGGER.info("Committee assignment job completed successfully");
            } catch (Exception e) {
                LOGGER.severe("Error in committee assignment: " + e.getMessage());
                e.printStackTrace();
                
                // Fail the job
                client.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage("Committee assignment failed: " + e.getMessage())
                    .send();
            }
        }
    }
    
    /**
     * Poll for new tasks every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void pollForNewTasks() {
        try {
            LOGGER.info("Polling for new tasks...");
            
            // Get all CREATED tasks
            TaskList taskList = taskListClient.getTasks(false, TaskState.CREATED, 10);
            
            if (taskList.getItems() != null && !taskList.getItems().isEmpty()) {
                LOGGER.info("Found " + taskList.getItems().size() + " tasks in CREATED state");
                
                for (Task task : taskList.getItems()) {
                    String taskId = task.getId();
                    
                    // Skip if we've already processed this task
                    if (processedTaskIds.contains(taskId)) {
                        continue;
                    }
                    
                    LOGGER.info("Processing new task: " + taskId + ", definition: " + task.getTaskDefinitionId());
                    
                    try {
                        // Get the process instance ID
                        String processInstanceId = task.getProcessInstanceKey();
                        
                        // Get the candidate groups directly from the task
                        List<String> candidateGroups = task.getCandidateGroups();
                        
                        if (candidateGroups != null && !candidateGroups.isEmpty()) {
                            String group = candidateGroups.getFirst();
                            
                            // Check if this is a legal, finance, or head task
                            if (group.equals("legal") || group.equals("finance") || group.equals("head")) {
                                // Get the committee assigned to this process instance
                                String committee = roundRobinService.getCommitteeForProcess(processInstanceId);
                                
                                if (committee != null) {
                                    LOGGER.info("Found committee " + committee + " for process " + processInstanceId);
                                    
                                    // Get a user who is in both the functional group and the committee
                                    String user = roundRobinService.getNextUserInGroupAndCommittee(group, committee);
                                    
                                    if (user != null) {
                                        // Assign the task to this user
                                        taskListClient.claim(taskId, user);
                                        LOGGER.info("Assigned task " + taskId + " to user " + user + " from group " + group + " and committee " + committee);
                                    } else {
                                        LOGGER.warning("No users found in both " + group + " and " + committee + ". Using regular assignment.");
                                        taskAssignmentService.assignTaskToRole(taskId, group);
                                    }
                                } else {
                                    LOGGER.warning("No committee found for process " + processInstanceId + ". Using regular assignment.");
                                    taskAssignmentService.assignTaskToRole(taskId, group);
                                }
                            } else {
                                // For other groups, use the standard assignment
                                taskAssignmentService.assignTaskToRole(taskId, group);
                                LOGGER.info("Assigned task " + taskId + " to a user from group " + group);
                            }
                        } else {
                            // If no candidate groups, use the task type
                            LOGGER.info("No candidate groups found for task " + taskId);
                            taskAssignmentService.assignTaskToRole(taskId, "users");
                        }
                        
                        // Mark as processed
                        processedTaskIds.add(taskId);
                        
                        // Limit the size of the processed set to avoid memory issues
                        if (processedTaskIds.size() > 1000) {
                            // Remove the oldest entries (this is a simple approach)
                            processedTaskIds.clear();
                            LOGGER.info("Cleared processed tasks cache");
                        }
                    } catch (Exception e) {
                        LOGGER.severe("Error assigning task " + taskId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                LOGGER.info("No new tasks found");
            }
        } catch (TaskListException e) {
            LOGGER.severe("Error polling for tasks: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Unexpected error in task polling: " + e.getMessage());
            e.printStackTrace();
        }
    }
}