package com.example.camunda2.registration_process.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.camunda.operate.model.ProcessInstance;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

public class ErrorHandlingServiceTests {

    private ErrorHandlingService errorHandlingService;
    private Exception testException;
    private TaskListException taskListException;

    @BeforeEach
    void setUp() {
        errorHandlingService = new ErrorHandlingService();
        testException = new Exception("Test error message");
        taskListException = new TaskListException("Task list error");
    }

    @Test
    void createErrorResponse_withDefaultStatus() {
        // Act
        ResponseEntity<String> response = errorHandlingService.createErrorResponse(testException);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error: Test error message", response.getBody());
    }
    
    @Test
    void createErrorResponse_withCustomStatus() {
        // Act
        ResponseEntity<String> response = errorHandlingService.createErrorResponse(testException, HttpStatus.BAD_REQUEST);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: Test error message", response.getBody());
    }
    
    @Test
    void handleTaskException() {
        // Act
        ResponseEntity<Task> response = errorHandlingService.handleTaskException(taskListException);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void handleTaskListException() {
        // Act
        ResponseEntity<TaskList> response = errorHandlingService.handleTaskListException(taskListException);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void handleProcessInstanceException() {
        // Act
        ResponseEntity<ProcessInstance> response = errorHandlingService.handleProcessInstanceException(testException);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void handleProcessInstanceEventException() {
        // Act
        ResponseEntity<ProcessInstanceEvent> response = errorHandlingService.handleProcessInstanceEventException(testException);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void handleVoidException() {
        // Act
        ResponseEntity<Void> response = errorHandlingService.handleVoidException(taskListException);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void createErrorResponse_withNullException() {
        // Arrange
        Exception nullException = new Exception();
        
        // Act
        ResponseEntity<String> response = errorHandlingService.createErrorResponse(nullException);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error: null", response.getBody());
    }
    
    @Test
    void createErrorResponse_withEmptyMessage() {
        // Arrange
        Exception emptyException = new Exception("");
        
        // Act
        ResponseEntity<String> response = errorHandlingService.createErrorResponse(emptyException);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error: ", response.getBody());
    }
}