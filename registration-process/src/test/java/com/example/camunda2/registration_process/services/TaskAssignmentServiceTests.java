package com.example.camunda2.registration_process.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;

@ExtendWith(MockitoExtension.class)
public class TaskAssignmentServiceTests {

    @Mock
    private CamundaTaskListClient taskListClient;
    
    @Mock
    private KeycloakService keycloakService;
    
    @Mock
    private Task mockTask;

    private TaskAssignmentService taskAssignmentService;

    @BeforeEach
    void setUp() {
        taskAssignmentService = new TaskAssignmentService(taskListClient, keycloakService);
    }

    @Test
    void getNextUserForTask_returnsUsersInRoundRobinFashion() {
        // Arrange
        String taskType = "Validator action";
        when(keycloakService.getNextUserFromGroup("validators")).thenReturn("user1", "user2", "user3", "user1");
        
        // Act & Assert - should cycle through users
        assertEquals("user1", taskAssignmentService.getNextUserForTask(taskType));
        assertEquals("user2", taskAssignmentService.getNextUserForTask(taskType));
        assertEquals("user3", taskAssignmentService.getNextUserForTask(taskType));
        // Should start over
        assertEquals("user1", taskAssignmentService.getNextUserForTask(taskType));
    }

    @Test
    void assignTaskRoundRobin_successfully() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        String taskType = "Validator action";
        
        when(taskListClient.getTask(taskId)).thenReturn(mockTask);
        when(mockTask.getTaskDefinitionId()).thenReturn(taskType);
        when(keycloakService.getNextUserFromGroup("validators")).thenReturn("user1");
        
        // Act
        taskAssignmentService.assignTaskRoundRobin(taskId);
        
        // Assert
        verify(taskListClient).claim(taskId, "user1");
    }

    @Test
    void assignTaskRoundRobin_throwsException_whenTaskListClientFails() throws TaskListException {
        // Arrange
        String taskId = "invalid-task";
        String taskType = "Validator action";
        
        when(taskListClient.getTask(taskId)).thenReturn(mockTask);
        when(mockTask.getTaskDefinitionId()).thenReturn(taskType);
        when(keycloakService.getNextUserFromGroup("validators")).thenReturn("user1");
        doThrow(new TaskListException("Failed to claim task")).when(taskListClient).claim(taskId, "user1");
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskAssignmentService.assignTaskRoundRobin(taskId);
        });
    }
    
    @Test
    void assignTaskToGroup_successfully() throws TaskListException {
        // Arrange
        String taskId = "task-456";
        String groupName = "admins";
        when(keycloakService.getNextUserFromGroup(groupName)).thenReturn("admin1");
        
        // Act
        taskAssignmentService.assignTaskToGroup(taskId, groupName);
        
        // Assert
        verify(taskListClient).claim(taskId, "admin1");
    }
    
    @Test
    void assignTaskToGroup_throwsException_whenNoUsersInGroup() throws TaskListException {
        // Arrange
        String taskId = "task-789";
        String groupName = "empty-group";
        when(keycloakService.getNextUserFromGroup(groupName)).thenReturn(null);
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskAssignmentService.assignTaskToGroup(taskId, groupName);
        });
    }
    
    @Test
    void getGroupForTaskType_returnsCorrectGroup() {
        // Act & Assert
        assertEquals("validators", taskAssignmentService.getGroupForTaskType("Validator action"));
        assertEquals("admins", taskAssignmentService.getGroupForTaskType("Admin action"));
        assertEquals("users", taskAssignmentService.getGroupForTaskType("Unknown task type"));
    }
    
    @Test
    void setTaskTypeGroupMapping_updatesMapping() {
        // Arrange
        String taskType = "New task type";
        String groupName = "new-group";
        
        // Act
        taskAssignmentService.setTaskTypeGroupMapping(taskType, groupName);
        
        // Assert
        assertEquals(groupName, taskAssignmentService.getGroupForTaskType(taskType));
    }
}