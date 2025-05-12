package com.example.camunda2.registration_process.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.zeebe.client.ZeebeClient;

@Service
public class ProcessVariableService {

    private final ZeebeClient zeebeClient;
    private final CamundaTaskListClient taskListClient;

    public ProcessVariableService(ZeebeClient zeebeClient, CamundaTaskListClient taskListClient) {
        this.zeebeClient = zeebeClient;
        this.taskListClient = taskListClient;
    }

    public long addVariablesByTaskId(String taskId, Map<String, Object> variables) throws Exception {
        Task task = taskListClient.getTask(taskId);
        long processInstanceKey = Long.parseLong(task.getProcessInstanceKey());

        zeebeClient.newSetVariablesCommand(processInstanceKey)
                .variables(variables)
                .send()
                .join();

        return processInstanceKey;
    }
    
    public void addVariablesByProcessInstanceId(long processInstanceKey, Map<String, Object> variables) {
        zeebeClient.newSetVariablesCommand(processInstanceKey)
                .variables(variables)
                .send()
                .join();
    }
}