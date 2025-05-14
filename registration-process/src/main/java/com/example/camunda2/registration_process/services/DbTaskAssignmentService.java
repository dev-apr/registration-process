package com.example.camunda2.registration_process.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for assigning tasks using database-backed round-robin assignment
 */
@Service
public class DbTaskAssignmentService {

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
    }
}