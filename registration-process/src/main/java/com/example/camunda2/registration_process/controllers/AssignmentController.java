package com.example.camunda2.registration_process.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.camunda2.registration_process.services.RoundRobinAssignmentService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing the round-robin assignment functionality
 */
@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {

    private final RoundRobinAssignmentService roundRobinAssignmentService;

    @Autowired
    public AssignmentController(RoundRobinAssignmentService roundRobinAssignmentService) {
        this.roundRobinAssignmentService = roundRobinAssignmentService;
    }

    /**
     * Get the next user for a specific role using round-robin assignment
     * 
     * @param roleType The role type (admin, validator, financial-controller)
     * @return The username of the next user to be assigned
     */
    @GetMapping("/next/{roleType}")
    public ResponseEntity<Map<String, String>> getNextUserForRole(@PathVariable String roleType) {
        String nextUser = roundRobinAssignmentService.getNextUserForRole(roleType);
        
        Map<String, String> response = new HashMap<>();
        response.put("roleType", roleType);
        response.put("assignedUser", nextUser != null ? nextUser : "No users available for this role");
        
        return ResponseEntity.ok(response);
    }
}