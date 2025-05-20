package com.example.camunda2.registration_process.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service for handling round-robin assignment of tasks using MySQL database
 * for persistence of assignment state.
 */
@Service
public class RoundRobinAssignmentService {

    private final JdbcTemplate jdbcTemplate;
    private final KeycloakService keycloakService;
    
    // Store the selected committee for each process instance
    private final Map<String, String> processCommitteeMap = new HashMap<>();

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
     * Select a committee (c1 or c2) in round-robin fashion
     * 
     * @return The selected committee name
     */
    @Transactional
    public String selectNextCommittee() {
        // The two committees we're selecting between
        final String COMMITTEE_TYPE = "violation-committee";
        final String[] committees = {"c1", "c2"};
        
        // Get the last assigned committee
        String lastCommittee = getLastAssignedCommittee();
        
        // Determine the next committee
        String nextCommittee;
        if (lastCommittee == null || lastCommittee.equals(committees[1])) {
            nextCommittee = committees[0];
        } else {
            nextCommittee = committees[1];
        }
        
        // Update the database with the new committee assignment
        updateLastAssignedCommittee(nextCommittee);
        
        return nextCommittee;
    }
    
    /**
     * Store the selected committee for a process instance
     */
    public void storeCommitteeForProcess(String processInstanceId, String committee) {
        processCommitteeMap.put(processInstanceId, committee);
        
        // Also store in database for persistence across restarts
        try {
            // Check if record exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM process_committee_mapping WHERE process_instance_id = ?",
                Integer.class,
                processInstanceId
            );
            
            if (count != null && count > 0) {
                // Update existing record
                jdbcTemplate.update(
                    "UPDATE process_committee_mapping SET committee = ? WHERE process_instance_id = ?",
                    committee,
                    processInstanceId
                );
            } else {
                // Insert new record
                jdbcTemplate.update(
                    "INSERT INTO process_committee_mapping (process_instance_id, committee) VALUES (?, ?)",
                    processInstanceId,
                    committee
                );
            }
        } catch (Exception e) {
            System.err.println("Error storing committee for process: " + e.getMessage());
        }
    }
    
    /**
     * Get the committee assigned to a process instance
     */
    public String getCommitteeForProcess(String processInstanceId) {
        // First check in-memory cache
        if (processCommitteeMap.containsKey(processInstanceId)) {
            return processCommitteeMap.get(processInstanceId);
        }
        
        // If not in memory, check database
        try {
            String committee = jdbcTemplate.queryForObject(
                "SELECT committee FROM process_committee_mapping WHERE process_instance_id = ?",
                String.class,
                processInstanceId
            );
            
            // Cache the result
            if (committee != null) {
                processCommitteeMap.put(processInstanceId, committee);
            }
            
            return committee;
        } catch (Exception e) {
            // If no record exists or any other error, return null
            return null;
        }
    }
    
    /**
     * Get users who are in both the functional group and the specified committee
     */
    public List<String> getUsersInGroupAndCommittee(String group, String committee) {
        // Get users from both groups
        List<String> groupUsers = keycloakService.getUsersByGroup(group);
        List<String> committeeUsers = keycloakService.getUsersByGroup(committee);
        
        // Find the intersection
        List<String> intersectionUsers = new ArrayList<>();
        for (String user : groupUsers) {
            if (committeeUsers.contains(user)) {
                intersectionUsers.add(user);
            }
        }
        
        return intersectionUsers;
    }
    
    /**
     * Get the next user who is in both the functional group and the committee
     */
    @Transactional
    public String getNextUserInGroupAndCommittee(String group, String committee) {
        // Get users who are in both groups
        List<String> users = getUsersInGroupAndCommittee(group, committee);
        
        if (users.isEmpty()) {
            return null;
        }
        
        // Create a combined role key for tracking assignments
        String combinedRole = group + "-" + committee;
        
        // Get the last assigned user for this combined role
        String lastAssignedUser = getLastAssignedUser(combinedRole);
        
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
        updateLastAssignedUser(combinedRole, nextUser);
        
        return nextUser;
    }
    
    /**
     * Get the last assigned committee from the database
     */
    private String getLastAssignedCommittee() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT last_assigned_committee FROM committee_assignment_state WHERE id = 1",
                String.class
            );
        } catch (Exception e) {
            // If no record exists or any other error, return null
            return null;
        }
    }
    
    /**
     * Update the last assigned committee in the database
     */
    private void updateLastAssignedCommittee(String committee) {
        // Check if record exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM committee_assignment_state",
            Integer.class
        );
        
        if (count != null && count > 0) {
            // Update existing record
            jdbcTemplate.update(
                "UPDATE committee_assignment_state SET last_assigned_committee = ?, last_assigned_timestamp = CURRENT_TIMESTAMP WHERE id = 1",
                committee
            );
        } else {
            // Insert new record
            jdbcTemplate.update(
                "INSERT INTO committee_assignment_state (id, last_assigned_committee) VALUES (1, ?)",
                committee
            );
        }
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
}