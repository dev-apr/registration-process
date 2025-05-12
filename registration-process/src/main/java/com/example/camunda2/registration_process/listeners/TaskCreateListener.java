// package com.example.camunda2.registration_process.listeners;

// import org.springframework.stereotype.Component;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;

// import com.example.camunda2.registration_process.services.TaskService;
// import com.example.camunda2.registration_process.services.TaskAssignmentService;

// import io.camunda.tasklist.dto.Task;
// import io.camunda.tasklist.dto.TaskList;
// import io.camunda.tasklist.dto.TaskState;
// import io.camunda.tasklist.exception.TaskListException;

// import java.util.logging.Logger;
// import java.util.logging.Level;

// @Component
// public class TaskCreateListener {

//     private static final Logger LOGGER = Logger.getLogger(TaskCreateListener.class.getName());

//     @Autowired
//     private TaskService taskService;
    
//     @Autowired
//     private TaskAssignmentService taskAssignmentService;

//     /**
//      * Scheduled method that runs every 10 seconds to check for unassigned tasks
//      * and assign them using round-robin strategy
//      */
//     @Scheduled(fixedRate = 10000)
//     public void checkAndAssignTasks() {
//         try {
//             LOGGER.info("Checking for unassigned tasks...");
//             TaskList unassignedTasks = taskService.getUnassignedTasks();
            
//             if (unassignedTasks == null || unassignedTasks.getItems() == null || unassignedTasks.getItems().isEmpty()) {
//                 LOGGER.info("No unassigned tasks found");
//                 return;
//             }
            
//             LOGGER.info("Found " + unassignedTasks.getItems().size() + " unassigned tasks");
            
//             for (Task task : unassignedTasks.getItems()) {
//                 try {
//                     // Only process tasks in CREATED state
//                     if (task.getTaskState() == TaskState.CREATED) {
//                         LOGGER.info("Assigning task " + task.getId() + " of type " + task.getTaskDefinitionId());
//                         taskAssignmentService.assignTaskRoundRobin(task.getId());
//                     }
//                 } catch (Exception e) {
//                     LOGGER.log(Level.WARNING, "Failed to assign task " + task.getId(), e);
//                 }
//             }
//         } catch (TaskListException e) {
//             LOGGER.log(Level.SEVERE, "Error checking for unassigned tasks", e);
//         }
//     }
// }