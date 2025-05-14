package com.example.camunda2.registration_process.services;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
    private String group;
    
    // Keep track of tasks we've already processed to avoid duplicates
    private final Set<String> processedTaskIds = new HashSet<>();
    
    @Autowired
    public TaskPollingService(
            CamundaTaskListClient taskListClient,
            DbTaskAssignmentService taskAssignmentService) {
        this.taskListClient = taskListClient;
        this.taskAssignmentService = taskAssignmentService;
        LOGGER.info("TaskPollingService initialized");
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
                        // Get the candidate groups directly from the task
                        List<String> candidateGroups = task.getCandidateGroups();
                        

                        if(candidateGroups.contains("legal") || candidateGroups.contains("finance") || candidateGroups.contains("head")){
                            group = "c1/" + candidateGroups.getFirst(); 
                        }
                        else{
                            group = candidateGroups.getFirst();
                        }
                        
                        // if (candidateGroups != null && !candidateGroups.isEmpty()) {
                        //     // Use the first candidate group (we could implement more complex logic if needed)
                        //     String group = candidateGroups.get(0);
                        //     LOGGER.info("Found candidate group for task: " + group);
                            
                        //     // Assign the task to a user from this group
                            taskAssignmentService.assignTaskToRole(taskId, group);
                        //     LOGGER.info("Successfully assigned task: " + taskId + " to a user from group: " + group);
                        // } else {
                        //     // Fallback to task type mapping if no candidate groups are specified
                        //     String taskType = task.getTaskDefinitionId();
                        //     String role = taskAssignmentService.getRoleForTaskType(taskType);
                        //     LOGGER.info("No candidate groups found. Determined role from task type: " + role);
                            
                        //     // Assign the task using role-based assignment
                        //     taskAssignmentService.assignTaskToRole(taskId, role);
                        //     LOGGER.info("Successfully assigned task: " + taskId + " to role: " + role);
                        // }
                        
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