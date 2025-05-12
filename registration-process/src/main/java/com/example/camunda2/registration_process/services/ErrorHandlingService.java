package com.example.camunda2.registration_process.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.camunda.operate.model.ProcessInstance;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

/**
 * Service for standardized error handling across the application
 */
@Service
public class ErrorHandlingService {

    /**
     * Create a standardized error response
     * 
     * @param e The exception that occurred
     * @return A ResponseEntity with appropriate status and error message
     */
    public ResponseEntity<String> createErrorResponse(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
    }
    
    /**
     * Create a standardized error response with a custom status
     * 
     * @param e The exception that occurred
     * @param status The HTTP status to return
     * @return A ResponseEntity with the specified status and error message
     */
    public ResponseEntity<String> createErrorResponse(Exception e, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body("Error: " + e.getMessage());
    }
    
    /**
     * Handle TaskListException for Task responses
     * 
     * @param e The TaskListException
     * @return A ResponseEntity with NOT_FOUND status
     */
    public ResponseEntity<Task> handleTaskException(TaskListException e) {
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Handle TaskListException for TaskList responses
     * 
     * @param e The TaskListException
     * @return A ResponseEntity with NOT_FOUND status
     */
    public ResponseEntity<TaskList> handleTaskListException(TaskListException e) {
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Handle Exception for ProcessInstance responses
     * 
     * @param e The Exception
     * @return A ResponseEntity with NOT_FOUND status
     */
    public ResponseEntity<ProcessInstance> handleProcessInstanceException(Exception e) {
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Handle Exception for ProcessInstanceEvent responses
     * 
     * @param e The Exception
     * @return A ResponseEntity with NOT_FOUND status
     */
    public ResponseEntity<ProcessInstanceEvent> handleProcessInstanceEventException(Exception e) {
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Handle TaskListException for void responses
     * 
     * @param e The TaskListException
     * @return A ResponseEntity with NOT_FOUND status
     */
    public ResponseEntity<Void> handleVoidException(TaskListException e) {
        return ResponseEntity.notFound().build();
    }
}