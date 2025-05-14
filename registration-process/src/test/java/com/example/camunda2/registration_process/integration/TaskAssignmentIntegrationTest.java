package com.example.camunda2.registration_process.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.camunda2.registration_process.services.KeycloakService;
import com.example.camunda2.registration_process.services.RoundRobinAssignmentService;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CamundaTaskListClient taskListClient;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private Task mockTask;

    @Test
    public void testAssignTask() throws Exception {
        // Arrange
        String taskId = "task-123";
        String taskType = "Admin action";
        
        when(taskListClient.getTask(taskId)).thenReturn(mockTask);
        when(mockTask.getTaskDefinitionId()).thenReturn(taskType);
        when(keycloakService.getUsersByGroup("admins")).thenReturn(Arrays.asList("admin1", "admin2"));
        
        // Act & Assert
        mockMvc.perform(post("/api/task-assignment/assign/{taskId}", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Task assigned successfully"));
        
        verify(taskListClient, times(1)).claim(eq(taskId), anyString());
    }

    @Test
    public void testAssignTaskToRole() throws Exception {
        // Arrange
        String taskId = "task-456";
        String role = "validator";
        
        when(keycloakService.getUsersByGroup("validators")).thenReturn(Arrays.asList("validator1", "validator2"));
        
        // Act & Assert
        mockMvc.perform(post("/api/task-assignment/assign/{taskId}/role/{role}", taskId, role)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Task assigned to role successfully"));
        
        verify(taskListClient, times(1)).claim(eq(taskId), anyString());
    }
}