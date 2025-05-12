package com.example.camunda2.registration_process.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.camunda2.registration_process.dto.ProcessRequest;
import com.example.camunda2.registration_process.services.ProcessService;
import com.example.camunda2.registration_process.services.ProcessVariableService;

import io.camunda.operate.model.ProcessInstance;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@ExtendWith(MockitoExtension.class)
public class ProcessControllerTests {

    @Mock
    private ProcessService processService;
    
    @Mock
    private ProcessVariableService processVariableService;

    private ProcessController processController;

    private ProcessInstanceEvent processInstanceEvent;
    private ProcessRequest processRequest;
    private ProcessInstance processInstance;
    private Map<String, Object> variables;

    @BeforeEach
    void setUp() {
        // Create the controller with the mocked dependencies
        processController = new ProcessController(processService, processVariableService);
        
        // Setup ProcessRequest
        processRequest = new ProcessRequest();
        processRequest.setWage(5000);
        processRequest.setDate("2023-01-01");

        // Setup ProcessInstanceEvent
        processInstanceEvent = mock(ProcessInstanceEvent.class);
        
        // Setup ProcessInstance - using mock instead of direct instantiation
        processInstance = mock(ProcessInstance.class);
        
        
        
        // Setup variables
        variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("comments", "Looks good");

    }
    @Test
    void startProcess_successfully() {
        // Arrange
        when(processService.startLoanApplication(any(ProcessRequest.class))).thenReturn(processInstanceEvent);

        ResponseEntity<ProcessInstanceEvent> response = processController.startProcess(processRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(processInstanceEvent, response.getBody());
        verify(processService).startLoanApplication(processRequest);
    }

    @Test
    void getProcessInstanceByKey_successfully() throws Exception {
        // Arrange
        when(processService.getProcessInstanceByKey(anyLong())).thenReturn(processInstance);

        // Act
        ResponseEntity<ProcessInstance> response = processController.getProcessInstanceByKey(123L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(processInstance, response.getBody());
        verify(processService).getProcessInstanceByKey(123L);
    }

    @Test
    void addVariableByTaskId_successfully() throws Exception {
        // Arrange
        when(processVariableService.addVariablesByTaskId("task-123", variables)).thenReturn(123L);

        // Act
        ResponseEntity<String> response = processController.addVariableByTaskId("task-123", variables);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Variables added to process instance 123", response.getBody());
        verify(processVariableService).addVariablesByTaskId("task-123", variables);
    }
    
    @Test
    void addVariableByTaskId_throwsException() throws Exception {
        // Arrange
        Exception exception = new RuntimeException("Task not found");
        when(processVariableService.addVariablesByTaskId("task-123", variables)).thenThrow(exception);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            processController.addVariableByTaskId("task-123", variables);
        });
        verify(processVariableService).addVariablesByTaskId("task-123", variables);
    }

    @Test
    void addVariableByProcessInstanceId_successfully() throws Exception {
        // Act
        ResponseEntity<String> response = processController.addVariableByProcessInstanceId(123L, variables);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Variables added to process instance 123", response.getBody());
        verify(processVariableService).addVariablesByProcessInstanceId(123L, variables);
    }
    
    @Test
    void addVariableByProcessInstanceId_throwsException() {
        // Arrange
        Exception exception = new RuntimeException("Test exception");
        doThrow(exception).when(processVariableService).addVariablesByProcessInstanceId(123L, variables);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            processController.addVariableByProcessInstanceId(123L, variables);
        });
        verify(processVariableService).addVariablesByProcessInstanceId(123L, variables);
    }
}