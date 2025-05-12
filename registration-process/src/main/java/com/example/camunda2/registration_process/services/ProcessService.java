package com.example.camunda2.registration_process.services;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessInstance;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

import org.springframework.stereotype.Service;

import com.example.camunda2.registration_process.dto.ProcessRequest;

@Service
public class ProcessService {

    private final ZeebeClient zeebeClient;
    private final CamundaOperateClient operateClient;

    public ProcessService(ZeebeClient zeebeClient, CamundaOperateClient operateClient) {
        this.zeebeClient = zeebeClient;
        this.operateClient = operateClient;
    }

    public ProcessInstanceEvent startLoanApplication(ProcessRequest request) {
        return zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_registrationProcess")
                .latestVersion()
                .variables(request)
                .send()
                .join();
    }
    
    public ProcessInstance getProcessInstanceByKey(Long processInstanceKey) throws OperateException {
        return operateClient.getProcessInstance(processInstanceKey);
    }
}
