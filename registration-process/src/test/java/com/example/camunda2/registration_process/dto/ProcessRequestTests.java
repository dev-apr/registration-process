package com.example.camunda2.registration_process.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ProcessRequestTests {

    @Test
    void gettersAndSetters_workCorrectly() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        
        // Act
        request.setWage(5000);
        request.setDate("2023-01-01");
        
        // Assert
        assertEquals(5000, request.getWage());
        assertEquals("2023-01-01", request.getRequestDate());
    }
}