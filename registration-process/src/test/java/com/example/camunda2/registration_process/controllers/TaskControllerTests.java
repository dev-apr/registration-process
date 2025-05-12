package com.example.camunda2.registration_process.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.camunda2.registration_process.services.TaskService;

import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.exception.TaskListException;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTests {

    @Mock
    private TaskService taskService;

    private TaskController taskController;

    private Task task;
    private TaskList taskList;
    private Map<String, Object> variables;
    private TaskListException taskListException;

    @BeforeEach
    void setUp() {
        // Create the controller with the mocked dependencies
        taskController = new TaskController(taskService);
        
        // Setup Task - using lenient() to avoid "unnecessary stubbing" errors
        task = mock(Task.class);
        
        // Setup TaskList
        taskList = mock(TaskList.class);
        
        // Setup variables
        variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("comments", "Looks good");
        
        // Setup exception
        taskListException = new TaskListException("Test exception");
    }

    @Test
    void getTasksByUser_successfully() throws TaskListException {
        // Arrange
        String username = "testUser";
        when(taskService.getTasksByUser(username)).thenReturn(taskList);

        // Act
        ResponseEntity<TaskList> response = taskController.getTasksByUser(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskList, response.getBody());
        verify(taskService).getTasksByUser(username);
    }
    
    @Test
    void getTasksByUser_throwsException() throws TaskListException {
        // Arrange
        String username = "testUser";
        when(taskService.getTasksByUser(username)).thenThrow(taskListException);

        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskController.getTasksByUser(username);
        });
        verify(taskService).getTasksByUser(username);
    }

    @Test
    void getTaskById_successfully() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        when(taskService.getTaskById(taskId)).thenReturn(task);

        // Act
        ResponseEntity<Task> response = taskController.getTaskById(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(task, response.getBody());
        verify(taskService).getTaskById(taskId);
    }
    
    @Test
    void getTaskById_throwsException() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        when(taskService.getTaskById(taskId)).thenThrow(taskListException);

        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskController.getTaskById(taskId);
        });
        verify(taskService).getTaskById(taskId);
    }

    @Test
    void getUnassignedTasks_successfully() throws TaskListException {
        // Arrange
        when(taskService.getUnassignedTasks()).thenReturn(taskList);

        // Act
        ResponseEntity<TaskList> response = taskController.getUnassignedTasks();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskList, response.getBody());
        verify(taskService).getUnassignedTasks();
    }
    
    @Test
    void getUnassignedTasks_throwsException() throws TaskListException {
        // Arrange
        when(taskService.getUnassignedTasks()).thenThrow(taskListException);

        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskController.getUnassignedTasks();
        });
        verify(taskService).getUnassignedTasks();
    }

    @Test
    void claimTask_successfully() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        doNothing().when(taskService).claimTaskRoundRobin(taskId);

        // Act
        ResponseEntity<String> response = taskController.claimTask(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task assigned using round robin", response.getBody());
        verify(taskService).claimTaskRoundRobin(taskId);
    }
    
    @Test
    void claimTask_throwsException() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        doThrow(taskListException).when(taskService).claimTaskRoundRobin(taskId);

        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskController.claimTask(taskId);
        });
        verify(taskService).claimTaskRoundRobin(taskId);
    }

    @Test
    void unclaimTask_successfully() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        lenient().when(task.getId()).thenReturn("task-123");
        lenient().when(task.getName()).thenReturn("Test Task");
        when(taskService.unclaimTask(taskId)).thenReturn(task);

        // Act
        ResponseEntity<Task> response = taskController.unclaimTask(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(task, response.getBody());
        verify(taskService).unclaimTask(taskId);
    }
    
    @Test
    void unclaimTask_throwsException() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        when(taskService.unclaimTask(taskId)).thenThrow(taskListException);

        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskController.unclaimTask(taskId);
        });
        verify(taskService).unclaimTask(taskId);
    }

    @Test
    void completeTask_successfully() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        doNothing().when(taskService).completeTask(taskId, variables);

        // Act
        ResponseEntity<Void> response = taskController.completeTask(taskId, variables);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskService).completeTask(taskId, variables);
    }
    
    @Test
    void completeTask_throwsException() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        doThrow(taskListException).when(taskService).completeTask(taskId, variables);

        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskController.completeTask(taskId, variables);
        });
        verify(taskService).completeTask(taskId, variables);
    }
    
    @Test
    void getTasksByUser_withEmptyUsername() throws TaskListException {
        // Arrange
        String username = "";
        when(taskService.getTasksByUser(username)).thenReturn(taskList);

        // Act
        ResponseEntity<TaskList> response = taskController.getTasksByUser(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskList, response.getBody());
        verify(taskService).getTasksByUser(username);
    }
    
    @Test
    void getTaskById_withEmptyTaskId() throws TaskListException {
        // Arrange
        String taskId = "";
        when(taskService.getTaskById(taskId)).thenReturn(task);

        // Act
        ResponseEntity<Task> response = taskController.getTaskById(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(task, response.getBody());
        verify(taskService).getTaskById(taskId);
    }
    
    @Test
    void completeTask_withEmptyVariables() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        Map<String, Object> emptyVariables = new HashMap<>();
        doNothing().when(taskService).completeTask(taskId, emptyVariables);

        // Act
        ResponseEntity<Void> response = taskController.completeTask(taskId, emptyVariables);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskService).completeTask(taskId, emptyVariables);
    }
    
    @Test
    void completeTask_withNullVariables() throws TaskListException {
        // Arrange
        String taskId = "task-123";
        Map<String, Object> nullVariables = null;
        doThrow(new IllegalArgumentException("Variables cannot be null")).when(taskService).completeTask(taskId, nullVariables);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskController.completeTask(taskId, nullVariables);
        });
        verify(taskService).completeTask(taskId, nullVariables);
    }
}