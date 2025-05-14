package com.example.camunda2.registration_process.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoundRobinAssignmentServiceTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private RoundRobinAssignmentService roundRobinService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetNextUserForRole_WithExistingUsers() {
        // Arrange
        String roleType = "admin";
        List<String> users = Arrays.asList("user1", "user2", "user3");
        
        when(keycloakService.getUsersByGroup("admins")).thenReturn(users);
        when(jdbcTemplate.queryForObject(
                eq("SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?"),
                eq(String.class),
                eq(roleType)
        )).thenReturn("user1");
        
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?"),
                eq(Integer.class),
                eq(roleType)
        )).thenReturn(1);

        // Act
        String nextUser = roundRobinService.getNextUserForRole(roleType);

        // Assert
        assertEquals("user2", nextUser);
        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE role_assignment_state SET last_assigned_user_id = ?, last_assigned_timestamp = CURRENT_TIMESTAMP WHERE role_type = ?"),
                eq("user2"),
                eq(roleType)
        );
    }

    @Test
    public void testGetNextUserForRole_WithNoExistingRecord() {
        // Arrange
        String roleType = "validator";
        List<String> users = Arrays.asList("validator1", "validator2");
        
        when(keycloakService.getUsersByGroup("validators")).thenReturn(users);
        when(jdbcTemplate.queryForObject(
                eq("SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?"),
                eq(String.class),
                eq(roleType)
        )).thenReturn(null);
        
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?"),
                eq(Integer.class),
                eq(roleType)
        )).thenReturn(0);

        // Act
        String nextUser = roundRobinService.getNextUserForRole(roleType);

        // Assert
        assertEquals("validator1", nextUser);
        verify(jdbcTemplate, times(1)).update(
                eq("INSERT INTO role_assignment_state (role_type, last_assigned_user_id) VALUES (?, ?)"),
                eq(roleType),
                eq("validator1")
        );
    }

    @Test
    public void testGetNextUserForRole_WithNoUsers() {
        // Arrange
        String roleType = "financial-controller";
        
        when(keycloakService.getUsersByGroup("financial-controllers")).thenReturn(Collections.emptyList());

        // Act
        String nextUser = roundRobinService.getNextUserForRole(roleType);

        // Assert
        assertNull(nextUser);
    }

    @Test
    public void testGetNextUserForRole_WithLastUserAtEndOfList() {
        // Arrange
        String roleType = "admin";
        List<String> users = Arrays.asList("user1", "user2", "user3");
        
        when(keycloakService.getUsersByGroup("admins")).thenReturn(users);
        when(jdbcTemplate.queryForObject(
                eq("SELECT last_assigned_user_id FROM role_assignment_state WHERE role_type = ?"),
                eq(String.class),
                eq(roleType)
        )).thenReturn("user3");
        
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM role_assignment_state WHERE role_type = ?"),
                eq(Integer.class),
                eq(roleType)
        )).thenReturn(1);

        // Act
        String nextUser = roundRobinService.getNextUserForRole(roleType);

        // Assert
        assertEquals("user1", nextUser); // Should wrap around to the beginning
        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE role_assignment_state SET last_assigned_user_id = ?, last_assigned_timestamp = CURRENT_TIMESTAMP WHERE role_type = ?"),
                eq("user1"),
                eq(roleType)
        );
    }
}