package com.example.camunda2.registration_process.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service for assigning tasks using database-backed round-robin assignment
 */
@Service
public class DbTaskAssignmentService {
    private static final Logger LOGGER = Logger.getLogger(DbTaskAssignmentService.class.getName());

    private final CamundaTaskListClient taskListClient;
    private final RoundRobinAssignmentService roundRobinService;

    @Autowired
    public DbTaskAssignmentService(
            CamundaTaskListClient taskListClient,
            RoundRobinAssignmentService roundRobinService) {
        this.taskListClient = taskListClient;
        this.roundRobinService = roundRobinService;
    }

    /**
     * Assign a task to the next user in a specific role group
     */
    public void assignTaskToRole(String taskId, String role) throws TaskListException {
        String user = roundRobinService.getNextUserForRole(role);
        if (user == null) {
            throw new TaskListException("No users found for role: " + role);
        }
        taskListClient.claim(taskId, user);
        LOGGER.info("Assigned task " + taskId + " to user " + user + " from role " + role);
    }
    
    /**
     * Assign a task to a user who is in both the functional group and the committee
     */
    public void assignTaskToRoleAndCommittee(String taskId, String role, String committee) throws TaskListException {
        String user = roundRobinService.getNextUserInGroupAndCommittee(role, committee);
        
        if (user == null) {
            LOGGER.warning("No users found in both " + role + " and " + committee + ". Falling back to role-only assignment.");
            user = roundRobinService.getNextUserForRole(role);
            
            if (user == null) {
                throw new TaskListException("No users found for role: " + role);
            }
        }
        
        taskListClient.claim(taskId, user);
        LOGGER.info("Assigned task " + taskId + " to user " + user + " from role " + role + " and committee " + committee);
    }
    
    /**
     * Get the process instance ID for a task
     */
    public String getProcessInstanceId(String taskId) throws TaskListException {
        Task task = taskListClient.getTask(taskId);
        return task.getProcessInstanceKey();
    }
}