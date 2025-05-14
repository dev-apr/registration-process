package com.example.camunda2.registration_process.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.camunda2.registration_process.services.TaskPollingService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for manually triggering task polling
 */
@RestController
@RequestMapping("/api/task-polling")
public class TaskPollingController {

    private final TaskPollingService taskPollingService;

    @Autowired
    public TaskPollingController(TaskPollingService taskPollingService) {
        this.taskPollingService = taskPollingService;
    }

    /**
     * Manually trigger task polling
     */
    @GetMapping("/poll")
    public ResponseEntity<Map<String, Object>> pollForTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            taskPollingService.pollForNewTasks();
            
            response.put("status", "success");
            response.put("message", "Task polling triggered successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error triggering task polling: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Get status of the task polling service
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "active");
        response.put("pollingInterval", "5 seconds");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}