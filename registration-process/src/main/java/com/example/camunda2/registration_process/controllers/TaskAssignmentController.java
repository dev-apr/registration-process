package com.example.camunda2.registration_process.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.camunda2.registration_process.dto.TaskGroupMapping;
import com.example.camunda2.registration_process.services.KeycloakService;
import com.example.camunda2.registration_process.services.TaskAssignmentService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing task assignment configurations
 */
@RestController
@RequestMapping("/task-assignment")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;
    private final KeycloakService keycloakService;

    public TaskAssignmentController(TaskAssignmentService taskAssignmentService, KeycloakService keycloakService) {
        this.taskAssignmentService = taskAssignmentService;
        this.keycloakService = keycloakService;
    }

    /**
     * Set a task type to group mapping
     */
    @PostMapping("/mapping")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> setTaskTypeGroupMapping(@RequestBody TaskGroupMapping mapping) {
        taskAssignmentService.setTaskTypeGroupMapping(mapping.getTaskType(), mapping.getGroupName());
        return ResponseEntity.ok("Task type '" + mapping.getTaskType() + "' mapped to group '" + mapping.getGroupName() + "'");
    }

    /**
     * Get the group for a task type
     */
    @GetMapping("/mapping/{taskType}")
    @PreAuthorize("hasAnyRole('admin','user')")
    public ResponseEntity<TaskGroupMapping> getGroupForTaskType(@PathVariable String taskType) {
        String groupName = taskAssignmentService.getGroupForTaskType(taskType);
        return ResponseEntity.ok(new TaskGroupMapping(taskType, groupName));
    }

    /**
     * Refresh the Keycloak group cache
     */
    @PostMapping("/refresh-cache/{groupName}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> refreshGroupCache(@PathVariable String groupName) {
        keycloakService.refreshGroupCache(groupName);
        return ResponseEntity.ok("Cache refreshed for group: " + groupName);
    }

    /**
     * Clear all caches
     */
    @PostMapping("/clear-caches")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> clearAllCaches() {
        keycloakService.clearAllCaches();
        return ResponseEntity.ok("All caches cleared");
    }
}