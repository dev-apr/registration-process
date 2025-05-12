package com.example.camunda2.registration_process.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;

@Service
public class TaskAssignmentService {

    private final CamundaTaskListClient taskListClient;
    private final KeycloakService keycloakService;
    
    // Map to store task type to Keycloak group mappings
    private final Map<String, String> taskTypeToGroupMap = new HashMap<>();

    public TaskAssignmentService(CamundaTaskListClient taskListClient, KeycloakService keycloakService) {
        this.taskListClient = taskListClient;
        this.keycloakService = keycloakService;
        
        // Initialize task type to group mappings
        taskTypeToGroupMap.put("Validator action", "validators");
        taskTypeToGroupMap.put("Admin action", "admins");
        taskTypeToGroupMap.put("Financial controller action", "financial-controllers");
        taskTypeToGroupMap.put("Verify request", "verifiers");
        // Add more mappings as needed
    }

    /**
     * Get the next user for a task based on the task type
     */
    public String getNextUserForTask(String taskType) {
        String groupName = taskTypeToGroupMap.getOrDefault(taskType, "users");
        return keycloakService.getNextUserFromGroup(groupName);
    }
    
    /**
     * Assign a task to the next user in round-robin fashion based on task type
     */
    public void assignTaskRoundRobin(String taskId) throws TaskListException {
        try {
            // Get task details to determine the task type
            Task task = taskListClient.getTask(taskId);
            String taskType = task.getTaskDefinitionId();
            
            // Get the next user for this task type
            String user = getNextUserForTask(taskType);
            
            // If no user found, use a default user
            if (user == null) {
                user = "admin";
            }
            
            // Assign the task
            taskListClient.claim(taskId, user);
        } catch (Exception e) {
            System.err.println("Error assigning task: " + e.getMessage());
            throw new TaskListException("Failed to assign task: " + e.getMessage(), e);
        }
    }
    
    /**
     * Assign a task to the next user in a specific group
     */
    public void assignTaskToGroup(String taskId, String groupName) throws TaskListException {
        String user = keycloakService.getNextUserFromGroup(groupName);
        if (user == null) {
            throw new TaskListException("No users found in group: " + groupName);
        }
        taskListClient.claim(taskId, user);
    }
    
    /**
     * Get the group name for a specific task type
     */
    public String getGroupForTaskType(String taskType) {
        return taskTypeToGroupMap.getOrDefault(taskType, "users");
    }
    
    /**
     * Add or update a task type to group mapping
     */
    public void setTaskTypeGroupMapping(String taskType, String groupName) {
        taskTypeToGroupMap.put(taskType, groupName);
    }
}

