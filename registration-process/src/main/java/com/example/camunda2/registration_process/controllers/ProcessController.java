package com.example.camunda2.registration_process.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.camunda2.registration_process.dto.ProcessRequest;
import com.example.camunda2.registration_process.services.ProcessService;
import com.example.camunda2.registration_process.services.ProcessVariableService;

import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessInstance;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

/**
 * Controller for process-related operations
 */
@RestController
@RequestMapping("/process")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
public class ProcessController {

    private final ProcessService processService;
    private final ProcessVariableService processVariableService;

    public ProcessController(
            ProcessService processService,
            ProcessVariableService processVariableService) {
        this.processService = processService;
        this.processVariableService = processVariableService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('Default user role')")
    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
    public ResponseEntity<ProcessInstanceEvent> startProcess(@RequestBody ProcessRequest request) {
        ProcessInstanceEvent processInstance = processService.startLoanApplication(request);
        return ResponseEntity.ok(processInstance);
    }

    @GetMapping("/process-instance/{processInstanceKey}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ProcessInstance> getProcessInstanceByKey(@PathVariable Long processInstanceKey) throws OperateException {
        ProcessInstance processInstance = processService.getProcessInstanceByKey(processInstanceKey);
        return ResponseEntity.ok(processInstance);
    }

    @PostMapping("/add-variables/{taskId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> addVariableByTaskId(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) throws Exception {
        long processInstanceKey = processVariableService.addVariablesByTaskId(taskId, variables);
        return ResponseEntity.ok("Variables added to process instance " + processInstanceKey);
    }

    @PostMapping("/add-variables-by-process-instance/{processInstanceKey}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> addVariableByProcessInstanceId(
            @PathVariable long processInstanceKey,
            @RequestBody Map<String, Object> variables) throws Exception {
        processVariableService.addVariablesByProcessInstanceId(processInstanceKey, variables);
        return ResponseEntity.ok("Variables added to process instance " + processInstanceKey);
    }
}
