package com.example.camunda2.registration_process.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class RoundRobinAssignmentServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private RoundRobinAssignmentService roundRobinAssignmentService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetNextUserForRole_FirstAssignment() {
        // Mock data
        String roleType = "admin";
        List<String> users = Arrays.asList("user1", "user2", "user3");
        
        // Mock Keycloak service to return users
        when(keycloakService.getUsersByGroup("admins")).thenReturn(users);
        
        // Mock database to return null for last assigned user (first assignment)
        when(jdbcTemplate.queryForObject(
            eq("SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?"),
            eq(String.class),
            eq(roleType)
        )).thenReturn(null);
        
        // Mock count query
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?"),
            eq(Integer.class),
            eq(roleType)
        )).thenReturn(1);
        
        // Call the method
        String nextUser = roundRobinAssignmentService.getNextUserForRole(roleType);
        
        // Verify the result - should be the first user
        assertEquals("user1", nextUser);
    }
    
    @Test
    public void testGetNextUserForRole_RoundRobin() {
        // Mock data
        String roleType = "validator";
        List<String> users = Arrays.asList("user1", "user2", "user3");
        
        // Mock Keycloak service to return users
        when(keycloakService.getUsersByGroup("validators")).thenReturn(users);
        
        // Mock database to return the last assigned user
        when(jdbcTemplate.queryForObject(
            eq("SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?"),
            eq(String.class),
            eq(roleType)
        )).thenReturn("user2");
        
        // Mock count query
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?"),
            eq(Integer.class),
            eq(roleType)
        )).thenReturn(1);
        
        // Call the method
        String nextUser = roundRobinAssignmentService.getNextUserForRole(roleType);
        
        // Verify the result - should be the next user in the list
        assertEquals("user3", nextUser);
    }
    
    @Test
    public void testGetNextUserForRole_RoundRobinWrapAround() {
        // Mock data
        String roleType = "financial-controller";
        List<String> users = Arrays.asList("user1", "user2", "user3");
        
        // Mock Keycloak service to return users
        when(keycloakService.getUsersByGroup("financial-controllers")).thenReturn(users);
        
        // Mock database to return the last assigned user (last in the list)
        when(jdbcTemplate.queryForObject(
            eq("SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?"),
            eq(String.class),
            eq(roleType)
        )).thenReturn("user3");
        
        // Mock count query
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?"),
            eq(Integer.class),
            eq(roleType)
        )).thenReturn(1);
        
        // Call the method
        String nextUser = roundRobinAssignmentService.getNextUserForRole(roleType);
        
        // Verify the result - should wrap around to the first user
        assertEquals("user1", nextUser);
    }
}