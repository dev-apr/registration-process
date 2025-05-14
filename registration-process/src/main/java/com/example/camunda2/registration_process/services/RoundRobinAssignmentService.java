package com.example.camunda2.registration_process.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for handling round-robin assignment of tasks using MySQL database
 * for persistence of assignment state.
 */
@Service
public class RoundRobinAssignmentService {

    private final JdbcTemplate jdbcTemplate;
    private final KeycloakService keycloakService;

    @Autowired
    public RoundRobinAssignmentService(JdbcTemplate jdbcTemplate, KeycloakService keycloakService) {
        this.jdbcTemplate = jdbcTemplate;
        this.keycloakService = keycloakService;
    }

    /**
     * Get the next user for a specific role using round-robin assignment
     * 
     * @param roleType The role type (admin, validator, financial-controller)
     * @return The username of the next user to be assigned
     */
    @Transactional
    public String getNextUserForRole(String role) {
        // Map role types to Keycloak group names if needed
        // String groupName = mapRoleTypeToGroup(role);
        
        // Get all users with this role from Keycloak
        List<String> users = keycloakService.getUsersByGroup(role);
        
        if (users.isEmpty()) {
            return null;
        }
        
        // Get the last assigned user for this role from database
        String lastAssignedUser = getLastAssignedUser(role);
        
        // Find the index of the last assigned user
        int lastIndex = -1;
        if (lastAssignedUser != null) {
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).equals(lastAssignedUser)) {
                    lastIndex = i;
                    break;
                }
            }
        }
        
        // Calculate the next index (round-robin)
        int nextIndex = (lastIndex + 1) % users.size();
        String nextUser = users.get(nextIndex);
        
        // Update the database with the new assignment
        updateLastAssignedUser(role, nextUser);
        
        return nextUser;
    }
    
    /**
     * Get the last assigned user for a role from the database
     */
    private String getLastAssignedUser(String roleType) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?",
                String.class,
                roleType
            );
        } catch (Exception e) {
            // If no record exists or any other error, return null
            return null;
        }
    }
    
    /**
     * Update the last assigned user for a role in the database
     */
    private void updateLastAssignedUser(String roleType, String userId) {
        // Check if record exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?",
            Integer.class,
            roleType
        );
        
        if (count != null && count > 0) {
            // Update existing record
            jdbcTemplate.update(
                "UPDATE role_assignment_state SET last_assigned_user_id = ?, last_assigned_timestamp = CURRENT_TIMESTAMP WHERE role_type = ?",
                userId,
                roleType
            );
        } else {
            // Insert new record
            jdbcTemplate.update(
                "INSERT INTO role_assignment_state (role_type, last_assigned_user_id) VALUES (?, ?)",
                roleType,
                userId
            );
        }
    }
    
    /**
     * Map role types to Keycloak group names
     */
    // private String mapRoleTypeToGroup(String roleType) {
    //     switch (roleType.toLowerCase()) {
    //         case "admin":
    //             return "admins";
    //         case "validator":
    //             return "validators";
    //         case "financial-controller":
    //             return "financial-controllers";
    //         default:
    //             return roleType;
    //     }
    // }
}