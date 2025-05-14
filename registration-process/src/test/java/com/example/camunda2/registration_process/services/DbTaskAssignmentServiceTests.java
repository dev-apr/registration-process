// package com.example.camunda2.registration_process.services;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import io.camunda.tasklist.CamundaTaskListClient;
// import io.camunda.tasklist.dto.Task;
// import io.camunda.tasklist.exception.TaskListException;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// public class DbTaskAssignmentServiceTests {

//     @Mock
//     private CamundaTaskListClient taskListClient;

//     @Mock
//     private RoundRobinAssignmentService roundRobinService;

//     @Mock
//     private Task mockTask;

//     @InjectMocks
//     private DbTaskAssignmentService dbTaskAssignmentService;

//     @BeforeEach
//     public void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     public void testGetRoleForTaskType() {
//         // Test known task types
//         assertEquals("validator", dbTaskAssignmentService.getRoleForTaskType("Validator action"));
//         assertEquals("admin", dbTaskAssignmentService.getRoleForTaskType("Admin action"));
//         assertEquals("financial-controller", dbTaskAssignmentService.getRoleForTaskType("Financial controller action"));
        
//         // Test unknown task type
//         assertEquals("user", dbTaskAssignmentService.getRoleForTaskType("Unknown task type"));
//     }

//     @Test
//     public void testAssignTaskRoundRobin_Success() throws TaskListException {
//         // Arrange
//         String taskId = "task-123";
//         String taskType = "Admin action";
//         String role = "admin";
//         String user = "admin-user";
        
//         when(taskListClient.getTask(taskId)).thenReturn(mockTask);
//         when(mockTask.getTaskDefinitionId()).thenReturn(taskType);
//         when(roundRobinService.getNextUserForRole(role)).thenReturn(user);
        
//         // Act
//         dbTaskAssignmentService.assignTaskRoundRobin(taskId);
        
//         // Assert
//         verify(taskListClient, times(1)).claim(taskId, user);
//     }

//     @Test
//     public void testAssignTaskRoundRobin_NoUserFound() throws TaskListException {
//         // Arrange
//         String taskId = "task-123";
//         String taskType = "Admin action";
//         String role = "admin";
        
//         when(taskListClient.getTask(taskId)).thenReturn(mockTask);
//         when(mockTask.getTaskDefinitionId()).thenReturn(taskType);
//         when(roundRobinService.getNextUserForRole(role)).thenReturn(null);
        
//         // Act
//         dbTaskAssignmentService.assignTaskRoundRobin(taskId);
        
//         // Assert - should use "admin" as default user
//         verify(taskListClient, times(1)).claim(taskId, "admin");
//     }

//     @Test
//     public void testAssignTaskToRole_Success() throws TaskListException {
//         // Arrange
//         String taskId = "task-123";
//         String role = "validator";
//         String user = "validator-user";
        
//         when(roundRobinService.getNextUserForRole(role)).thenReturn(user);
        
//         // Act
//         dbTaskAssignmentService.assignTaskToRole(taskId, role);
        
//         // Assert
//         verify(taskListClient, times(1)).claim(taskId, user);
//     }

//     @Test
//     public void testAssignTaskToRole_NoUserFound() {
//         // Arrange
//         String taskId = "task-123";
//         String role = "validator";
        
//         when(roundRobinService.getNextUserForRole(role)).thenReturn(null);
        
//         // Act & Assert
//         TaskListException exception = assertThrows(TaskListException.class, () -> {
//             dbTaskAssignmentService.assignTaskToRole(taskId, role);
//         });
        
//         assertEquals("No users found for role: validator", exception.getMessage());
//     }

//     @Test
//     public void testSetTaskTypeRoleMapping() {
//         // Arrange
//         String taskType = "New task type";
//         String role = "new-role";
        
//         // Act
//         dbTaskAssignmentService.setTaskTypeRoleMapping(taskType, role);
        
//         // Assert - verify the mapping was set correctly
//         assertEquals(role, dbTaskAssignmentService.getRoleForTaskType(taskType));
//     }
// }