package com.example.camunda2.registration_process.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.camunda2.registration_process.services.ErrorHandlingService;

import io.camunda.tasklist.exception.TaskListException;

/**
 * Global exception handler for the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorHandlingService errorHandlingService;

    public GlobalExceptionHandler(ErrorHandlingService errorHandlingService) {
        this.errorHandlingService = errorHandlingService;
    }

    /**
     * Handle TaskListException
     * 
     * @param ex The exception
     * @return An appropriate response entity
     */
    @ExceptionHandler(TaskListException.class)
    public ResponseEntity<?> handleTaskListException(TaskListException ex) {
        return errorHandlingService.createErrorResponse(ex);
    }
    
    /**
     * Handle general exceptions
     * 
     * @param ex The exception
     * @return An appropriate response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return errorHandlingService.createErrorResponse(ex);
    }
}