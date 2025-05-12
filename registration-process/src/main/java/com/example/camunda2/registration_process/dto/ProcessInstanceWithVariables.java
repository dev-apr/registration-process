package com.example.camunda2.registration_process.dto;

import java.util.Map;

import io.camunda.operate.model.ProcessInstance;

/**
 * DTO that extends ProcessInstance to include variables
 */
public class ProcessInstanceWithVariables {
    
    private ProcessInstance processInstance;
    private Map<String, Object> variables;
    
    public ProcessInstanceWithVariables(ProcessInstance processInstance, Map<String, Object> variables) {
        this.processInstance = processInstance;
        this.variables = variables;
    }
    
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }
    
    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }
    
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}