package com.example.camunda2.registration_process.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;

/**
 * Extension of TaskService that adds database-backed round-robin assignment functionality
 * without modifying the original TaskService class.
 */
@Service
public class TaskServiceExtension {

    private final CamundaTaskListClient taskListClient;
    private final DbTaskAssignmentService dbTaskAssignmentService;

    @Autowired
    public TaskServiceExtension(
            CamundaTaskListClient taskListClient,
            DbTaskAssignmentService dbTaskAssignmentService) {
        this.taskListClient = taskListClient;
        this.dbTaskAssignmentService = dbTaskAssignmentService;
    }

    /**
     * Claim a task using database-backed round-robin assignment
     */
    // public Task claimTaskWithDbRoundRobin(String taskId) throws TaskListException {
    //     dbTaskAssignmentService.assignTaskRoundRobin(taskId);
    //     return taskListClient.getTask(taskId);
    // }

    /**
     * Claim a task for a specific role using database-backed round-robin assignment
     */
    public Task claimTaskForRole(String taskId, String role) throws TaskListException {
        dbTaskAssignmentService.assignTaskToRole(taskId, role);
        return taskListClient.getTask(taskId);
    }
}