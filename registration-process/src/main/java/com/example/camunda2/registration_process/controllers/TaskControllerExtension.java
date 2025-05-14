package com.example.camunda2.registration_process.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.camunda2.registration_process.services.TaskServiceExtension;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;

/**
 * Extension of TaskController that adds database-backed round-robin assignment functionality
 * without modifying the original TaskController class.
 */
@RestController
@RequestMapping("/tasks-db")
public class TaskControllerExtension {

    private final TaskServiceExtension taskServiceExtension;

    @Autowired
    public TaskControllerExtension(TaskServiceExtension taskServiceExtension) {
        this.taskServiceExtension = taskServiceExtension;
    }

    /**
     * Claim a task using database-backed round-robin assignment
     */
    // @PostMapping("/{taskId}/claim-db")
    // public ResponseEntity<Task> claimTaskWithDbRoundRobin(@PathVariable String taskId) throws TaskListException {
    //     Task task = taskServiceExtension.claimTaskWithDbRoundRobin(taskId);
    //     return ResponseEntity.ok(task);
    // }

    /**
     * Claim a task for a specific role using database-backed round-robin assignment
     */
    @PostMapping("/{taskId}/claim-role/{role}")
    public ResponseEntity<Task> claimTaskForRole(
            @PathVariable String taskId,
            @PathVariable String role) throws TaskListException {
        Task task = taskServiceExtension.claimTaskForRole(taskId, role);
        return ResponseEntity.ok(task);
    }
}