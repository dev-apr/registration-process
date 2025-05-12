package com.example.camunda2.registration_process.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.camunda2.registration_process.dto.ProcessRequest;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessInstance;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@ExtendWith(MockitoExtension.class)
public class ProcessServiceTests {

    @Mock
    private ZeebeClient zeebeClient;
    
    @Mock
    private CamundaOperateClient operateClient;

    private ProcessService processService;
    private ProcessRequest processRequest;
    private ProcessInstanceEvent processInstanceEvent;
    private ProcessInstance processInstance;

    @BeforeEach
    void setUp() {
        processService = new ProcessService(zeebeClient, operateClient);
        
        // Setup ProcessRequest
        processRequest = new ProcessRequest();
        processRequest.setWage(5000);
        processRequest.setDate("2023-01-01");
        
        // Setup ProcessInstanceEvent
        processInstanceEvent = mock(ProcessInstanceEvent.class);
        
        // Setup ProcessInstance
        processInstance = mock(ProcessInstance.class);
    }

    @Test
    void startLoanApplication_successfully() {
        // Arrange
        CreateProcessInstanceCommandStep1 step1 = mock(CreateProcessInstanceCommandStep1.class);
        CreateProcessInstanceCommandStep2 step2 = mock(CreateProcessInstanceCommandStep2.class);
        CreateProcessInstanceCommandStep3 step3 = mock(CreateProcessInstanceCommandStep3.class);
        ZeebeFuture<ProcessInstanceEvent> future = mock(ZeebeFuture.class);
        
        when(zeebeClient.newCreateInstanceCommand()).thenReturn(step1);
        when(step1.bpmnProcessId("Process_registrationProcess")).thenReturn(step2);
        when(step2.latestVersion()).thenReturn(step3);
        when(step3.variables(processRequest)).thenReturn(step3);
        when(step3.send()).thenReturn(future);
        when(future.join()).thenReturn(processInstanceEvent);
        
        // Act
        ProcessInstanceEvent result = processService.startLoanApplication(processRequest);
        
        // Assert
        assertEquals(processInstanceEvent, result);
        verify(zeebeClient).newCreateInstanceCommand();
    }
    
    @Test
    void getProcessInstanceByKey_successfully() throws OperateException {
        // Arrange
        Long processInstanceKey = 1000L;
        when(operateClient.getProcessInstance(processInstanceKey)).thenReturn(processInstance);
        
        // Act
        ProcessInstance result = processService.getProcessInstanceByKey(processInstanceKey);
        
        // Assert
        assertEquals(processInstance, result);
        verify(operateClient).getProcessInstance(processInstanceKey);
    }
    
    @Test
    void getProcessInstanceByKey_throwsException() throws OperateException {
        // Arrange
        Long processInstanceKey = 1000L;
        when(operateClient.getProcessInstance(processInstanceKey)).thenThrow(new OperateException("Process instance not found"));
        
        // Act & Assert
        assertThrows(OperateException.class, () -> {
            processService.getProcessInstanceByKey(processInstanceKey);
        });
        verify(operateClient).getProcessInstance(processInstanceKey);
    }
    
    @Test
    void startLoanApplication_withNullRequest() {
        // Arrange
        CreateProcessInstanceCommandStep1 step1 = mock(CreateProcessInstanceCommandStep1.class);
        CreateProcessInstanceCommandStep2 step2 = mock(CreateProcessInstanceCommandStep2.class);
        CreateProcessInstanceCommandStep3 step3 = mock(CreateProcessInstanceCommandStep3.class);
        ZeebeFuture<ProcessInstanceEvent> future = mock(ZeebeFuture.class);
        
        when(zeebeClient.newCreateInstanceCommand()).thenReturn(step1);
        when(step1.bpmnProcessId("Process_registrationProcess")).thenReturn(step2);
        when(step2.latestVersion()).thenReturn(step3);
        // Explicitly cast null to ProcessRequest to resolve ambiguity
        when(step3.variables((ProcessRequest) null)).thenReturn(step3);
        when(step3.send()).thenReturn(future);
        when(future.join()).thenReturn(processInstanceEvent);
        
        // Act
        ProcessInstanceEvent result = processService.startLoanApplication(null);
        
        // Assert
        assertEquals(processInstanceEvent, result);
        verify(zeebeClient).newCreateInstanceCommand();
    }
}