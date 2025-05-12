package com.example.camunda2.registration_process.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.SetVariablesCommandStep1;
import io.camunda.zeebe.client.api.command.SetVariablesCommandStep1.SetVariablesCommandStep2;
import io.camunda.zeebe.client.api.response.SetVariablesResponse;

@ExtendWith(MockitoExtension.class)
public class ProcessVariableServiceTests {

    @Mock
    private ZeebeClient zeebeClient;
    
    @Mock
    private CamundaTaskListClient taskListClient;
    
    private ProcessVariableService processVariableService;
    private Task task;
    private Map<String, Object> variables;
    private SetVariablesResponse setVariablesResponse;
    
    @BeforeEach
    void setUp() {
        processVariableService = new ProcessVariableService(zeebeClient, taskListClient);
        
        // Setup Task
        task = new Task();
        task.setId("task-123");
        task.setProcessInstanceKey("1000");
        
        // Setup variables
        variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("comments", "Approved");
        
        // Setup SetVariablesResponse
        setVariablesResponse = mock(SetVariablesResponse.class);
    }
    
    @Test
    void addVariablesByTaskId_successfully() throws Exception {
        // Arrange
        SetVariablesCommandStep1 step1 = mock(SetVariablesCommandStep1.class);
        SetVariablesCommandStep2 step2 = mock(SetVariablesCommandStep2.class);
        ZeebeFuture<SetVariablesResponse> future = mock(ZeebeFuture.class);
        
        when(taskListClient.getTask("task-123")).thenReturn(task);
        when(zeebeClient.newSetVariablesCommand(1000L)).thenReturn(step1);
        when(step1.variables(variables)).thenReturn(step2);
        when(step2.send()).thenReturn(future);
        when(future.join()).thenReturn(setVariablesResponse);
        
        // Act
        long result = processVariableService.addVariablesByTaskId("task-123", variables);
        
        // Assert
        assertEquals(1000L, result);
        verify(taskListClient).getTask("task-123");
        verify(zeebeClient).newSetVariablesCommand(1000L);
    }
    
    @Test
    void addVariablesByTaskId_throwsException_whenTaskNotFound() throws Exception {
        // Arrange
        when(taskListClient.getTask("non-existent-task")).thenThrow(new TaskListException("Task not found"));
        
        // Act & Assert
        assertThrows(TaskListException.class, () -> {
            processVariableService.addVariablesByTaskId("non-existent-task", variables);
        });
        verify(taskListClient).getTask("non-existent-task");
    }
    
    @Test
    void addVariablesByTaskId_throwsException_whenProcessInstanceKeyIsInvalid() throws Exception {
        // Arrange
        Task invalidTask = new Task();
        invalidTask.setId("task-123");
        invalidTask.setProcessInstanceKey("invalid-key");
        
        when(taskListClient.getTask("task-123")).thenReturn(invalidTask);
        
        // Act & Assert
        assertThrows(NumberFormatException.class, () -> {
            processVariableService.addVariablesByTaskId("task-123", variables);
        });
        verify(taskListClient).getTask("task-123");
    }
    
    @Test
    void addVariablesByProcessInstanceId_successfully() {
        // Arrange
        SetVariablesCommandStep1 step1 = mock(SetVariablesCommandStep1.class);
        SetVariablesCommandStep2 step2 = mock(SetVariablesCommandStep2.class);
        ZeebeFuture<SetVariablesResponse> future = mock(ZeebeFuture.class);
        
        when(zeebeClient.newSetVariablesCommand(1000L)).thenReturn(step1);
        when(step1.variables(variables)).thenReturn(step2);
        when(step2.send()).thenReturn(future);
        when(future.join()).thenReturn(setVariablesResponse);
        
        // Act
        processVariableService.addVariablesByProcessInstanceId(1000L, variables);
        
        // Assert
        verify(zeebeClient).newSetVariablesCommand(1000L);
    }
    
    @Test
    void addVariablesByProcessInstanceId_withEmptyVariables() {
        // Arrange
        Map<String, Object> emptyVariables = new HashMap<>();
        SetVariablesCommandStep1 step1 = mock(SetVariablesCommandStep1.class);
        SetVariablesCommandStep2 step2 = mock(SetVariablesCommandStep2.class);
        ZeebeFuture<SetVariablesResponse> future = mock(ZeebeFuture.class);
        
        when(zeebeClient.newSetVariablesCommand(1000L)).thenReturn(step1);
        when(step1.variables(emptyVariables)).thenReturn(step2);
        when(step2.send()).thenReturn(future);
        when(future.join()).thenReturn(setVariablesResponse);
        
        // Act
        processVariableService.addVariablesByProcessInstanceId(1000L, emptyVariables);
        
        // Assert
        verify(zeebeClient).newSetVariablesCommand(1000L);
    }
}