package com.example.camunda2.registration_process.controllers;

import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.exception.TaskListException;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.camunda2.registration_process.services.TaskService;

/**
 * Controller for task-related operations
 */
@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<TaskList> getTasksByUser(@PathVariable String username) throws TaskListException {
        TaskList tasks = taskService.getTasksByUser(username);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('admin','user')")
    public ResponseEntity<Task> getTaskById(@PathVariable String taskId) throws TaskListException {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<TaskList> getUnassignedTasks() throws TaskListException {
        TaskList tasks = taskService.getUnassignedTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<TaskList> getAllTasks() throws TaskListException {
        TaskList tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}/variables")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public ResponseEntity<List> getVariablesByTask(@PathVariable String taskId) throws TaskListException {
        List variables = taskService.getVariablesByTask(taskId);
        return ResponseEntity.ok(variables);
    }

    @PostMapping("/{taskId}/claim")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> claimTask(@PathVariable String taskId) throws TaskListException {
        taskService.claimTaskRoundRobin(taskId);
        return ResponseEntity.ok("Task assigned using round robin");
    }
    
    @PostMapping("/{taskId}/claim/group/{groupName}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> claimTaskForGroup(
            @PathVariable String taskId,
            @PathVariable String groupName) throws TaskListException {
        taskService.claimTaskForGroup(taskId, groupName);
        return ResponseEntity.ok("Task assigned to group " + groupName + " using round robin");
    }

    @PostMapping("/{taskId}/unclaim")
    @PreAuthorize("hasAnyRole('admin','user')")
    public ResponseEntity<Task> unclaimTask(@PathVariable String taskId) throws TaskListException {
        Task task = taskService.unclaimTask(taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasAnyRole('user','admin')")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) throws TaskListException {
        taskService.completeTask(taskId, variables);
        return ResponseEntity.ok().build();
    }
}