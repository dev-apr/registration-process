// package com.example.camunda2.registration_process.controllers;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.example.camunda2.registration_process.services.DbTaskAssignmentService;
// import io.camunda.tasklist.exception.TaskListException;

// import java.util.HashMap;
// import java.util.Map;

// /**
//  * Controller for task assignment operations
//  */
// @RestController
// @RequestMapping("/api/task-assignment")
// public class TaskAssignmentController {

//     private final DbTaskAssignmentService dbTaskAssignmentService;

//     @Autowired
//     public TaskAssignmentController(DbTaskAssignmentService dbTaskAssignmentService) {
//         this.dbTaskAssignmentService = dbTaskAssignmentService;
//     }

//     /**
//      * Assign a task using round-robin assignment based on task type
//      */
//     @PostMapping("/assign/{taskId}")
//     public ResponseEntity<?> assignTask(@PathVariable String taskId) {
//         try {
//             dbTaskAssignmentService.assignTaskRoundRobin(taskId);
//             Map<String, String> response = new HashMap<>();
//             response.put("status", "success");
//             response.put("message", "Task assigned successfully");
//             return ResponseEntity.ok(response);
//         } catch (TaskListException e) {
//             Map<String, String> error = new HashMap<>();
//             error.put("status", "error");
//             error.put("message", e.getMessage());
//             return ResponseEntity.badRequest().body(error);
//         }
//     }

//     /**
//      * Assign a task to a specific role using round-robin
//      */
//     @PostMapping("/assign/{taskId}/role/{role}")
//     public ResponseEntity<?> assignTaskToRole(
//             @PathVariable String taskId,
//             @PathVariable String role) {
//         try {
//             dbTaskAssignmentService.assignTaskToRole(taskId, role);
//             Map<String, String> response = new HashMap<>();
//             response.put("status", "success");
//             response.put("message", "Task assigned to role successfully");
//             return ResponseEntity.ok(response);
//         } catch (TaskListException e) {
//             Map<String, String> error = new HashMap<>();
//             error.put("status", "error");
//             error.put("message", e.getMessage());
//             return ResponseEntity.badRequest().body(error);
//         }
//     }
// }