package com.example.camunda2.registration_process.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTests {

    @Mock
    private CamundaTaskListClient taskListClient;
    
    @Mock
    private TaskAssignmentService taskAssignmentService;
    
    private TaskService taskService;
    
    private Task task;
    private TaskList taskList;
    private Map<String, Object> variables;
    
    @Captor
    private ArgumentCaptor<TaskSearch> taskSearchCaptor;
    
    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskListClient, taskAssignmentService);
        
        // Setup Task
        task = new Task();
        task.setId("task-123");
        task.setName("Review Application");
        task.setAssignee("user1");
        
        // Setup TaskList
        taskList = new TaskList();
        
        // Setup variables
        variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("comments", "Approved");
    }
    
    @Test
    void getTasksByUser_successfully() throws TaskListException {
        // Arrange
        when(taskListClient.getTasks(any(TaskSearch.class))).thenReturn(taskList);
        
        // Act
        TaskList result = taskService.getTasksByUser("user1");
        
        // Assert
        assertEquals(taskList, result);
        verify(taskListClient).getTasks(taskSearchCaptor.capture());
        assertEquals("user1", taskSearchCaptor.getValue().getAssignee());
    }
    
    @Test
    void getTasksByUser_throwsException() throws TaskListException {
        // Arrange
        when(taskListClient.getTasks(any(TaskSearch.class))).thenThrow(new TaskListException("Failed to get tasks"));
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskService.getTasksByUser("user1");
        });
    }
    
    @Test
    void getTaskById_successfully() throws TaskListException {
        // Arrange
        when(taskListClient.getTask("task-123")).thenReturn(task);
        
        // Act
        Task result = taskService.getTaskById("task-123");
        
        // Assert
        assertEquals(task, result);
        verify(taskListClient).getTask("task-123");
    }
    
    @Test
    void getTaskById_throwsException() throws TaskListException {
        // Arrange
        when(taskListClient.getTask("non-existent-task")).thenThrow(new TaskListException("Task not found"));
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskService.getTaskById("non-existent-task");
        });
    }
    
    @Test
    void getUnassignedTasks_successfully() throws TaskListException {
        // Arrange
        when(taskListClient.getTasks(any(TaskSearch.class))).thenReturn(taskList);
        
        // Act
        TaskList result = taskService.getUnassignedTasks();
        
        // Assert
        assertEquals(taskList, result);
        verify(taskListClient).getTasks(taskSearchCaptor.capture());
        assertEquals(TaskState.CREATED, taskSearchCaptor.getValue().getState());
        assertEquals(null, taskSearchCaptor.getValue().getAssignee());
    }
    
    @Test
    void getUnassignedTasks_throwsException() throws TaskListException {
        // Arrange
        when(taskListClient.getTasks(any(TaskSearch.class))).thenThrow(new TaskListException("Failed to get tasks"));
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskService.getUnassignedTasks();
        });
    }
    
    @Test
    void claimTaskRoundRobin_successfully() throws TaskListException {
        // Act
        taskService.claimTaskRoundRobin("task-123");
        
        // Assert
        verify(taskAssignmentService).assignTaskRoundRobin("task-123");
    }
    
    @Test
    void claimTaskRoundRobin_throwsException() throws TaskListException {
        // Arrange
        doThrow(new TaskListException("Failed to claim task")).when(taskAssignmentService).assignTaskRoundRobin("task-123");
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskService.claimTaskRoundRobin("task-123");
        });
    }
    
    @Test
    void unclaimTask_successfully() throws TaskListException {
        // Arrange
        when(taskListClient.unclaim("task-123")).thenReturn(task);
        
        // Act
        Task result = taskService.unclaimTask("task-123");
        
        // Assert
        assertEquals(task, result);
        verify(taskListClient).unclaim("task-123");
    }
    
    @Test
    void unclaimTask_throwsException() throws TaskListException {
        // Arrange
        when(taskListClient.unclaim("task-123")).thenThrow(new TaskListException("Failed to unclaim task"));
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskService.unclaimTask("task-123");
        });
    }
    
    @Test
    void completeTask_successfully() throws TaskListException {
        // Act
        taskService.completeTask("task-123", variables);
        
        // Assert
        verify(taskListClient).completeTask("task-123", variables);
    }
    
    @Test
    void completeTask_throwsException() throws TaskListException {
        // Arrange
        doThrow(new TaskListException("Failed to complete task")).when(taskListClient).completeTask("task-123", variables);
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            taskService.completeTask("task-123", variables);
        });
    }
    
    @Test
    void completeTask_withEmptyVariables() throws TaskListException {
        // Arrange
        Map<String, Object> emptyVariables = new HashMap<>();
        
        // Act
        taskService.completeTask("task-123", emptyVariables);
        
        // Assert
        verify(taskListClient).completeTask("task-123", emptyVariables);
    }
}