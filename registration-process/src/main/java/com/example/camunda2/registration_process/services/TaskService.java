package com.example.camunda2.registration_process.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;

@Service
public class TaskService {

    private final CamundaTaskListClient taskListClient;
    private final TaskAssignmentService taskAssignmentService;

    public TaskService(CamundaTaskListClient taskListClient, TaskAssignmentService taskAssignmentService) {
        this.taskListClient = taskListClient;
        this.taskAssignmentService = taskAssignmentService;
    }

    public TaskList getTasksByUser(String username) throws TaskListException {
        TaskSearch search = new TaskSearch();
        search.setAssignee(username);
        return taskListClient.getTasks(search);
    }

    public Task getTaskById(String taskId) throws TaskListException {
        return taskListClient.getTask(taskId);
    }

    public List<Variable> getVariablesByTask(String taskId) throws TaskListException {
        return taskListClient.getVariables(taskId);
    }

    public TaskList getUnassignedTasks() throws TaskListException {
        TaskSearch search = new TaskSearch();
        search.setState(TaskState.CREATED);
        search.setAssignee(null);
        return taskListClient.getTasks(search);
    }

    public void claimTaskRoundRobin(String taskId) throws TaskListException {
        taskAssignmentService.assignTaskRoundRobin(taskId);
    }
    
    /**
     * Claim a task for a specific group using round-robin assignment
     */
    public void claimTaskForGroup(String taskId, String groupName) throws TaskListException {
        taskAssignmentService.assignTaskToGroup(taskId, groupName);
    }

    public Task unclaimTask(String taskId) throws TaskListException {
        return taskListClient.unclaim(taskId);
    }

    public void completeTask(String taskId, Map<String, Object> variables) throws TaskListException {
        taskListClient.completeTask(taskId, variables);
    }

    public TaskList getAllTasks() throws TaskListException {
                try {
                    System.out.println("Getting all tasks (admin view)");
                    
                    TaskSearch search = new TaskSearch();
                    Pagination pageSize = new Pagination().setPageSize(100);
                    search.setPagination(pageSize); // Increase to find more tasks
                    
                    TaskList tasks = taskListClient.getTasks(search);
                    
                    // Handle null tasks
                    if (tasks == null) {
                        System.out.println("WARNING: TaskList is null for all tasks");
                        TaskList emptyList = new TaskList();
                        emptyList.setItems(new ArrayList<>());
                        return emptyList;
                    }
                    
                    // Handle null tasks list
                    if (tasks.getItems() == null) {
                        System.out.println("WARNING: Tasks list is null for all tasks");
                        tasks.setItems(new ArrayList<>());
                        return tasks;
                    }
                    
                    // Ensure each task has creation time and variables set
                    for (Task task : tasks.getItems()) {
                        // Ensure creation time is set
                        if (task.getCreationDate() == null) {
                            task.setCreationDate(java.time.OffsetDateTime.now().toString());
                        }
                        
                        // Ensure variables is not null
                        if (task.getVariables() == null) {
                            task.setVariables(new ArrayList<>());
                        }
                    }
                    
                    System.out.println("Found " + tasks.getItems().size() + " tasks in total");
                    return tasks;
                } catch (Exception e) {
                    System.err.println("Error getting all tasks: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Return empty list instead of throwing exception
                    TaskList emptyList = new TaskList();
                    emptyList.setItems(new ArrayList<>());
                    return emptyList;
                }
            }
}