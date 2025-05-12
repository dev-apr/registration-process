package com.example.camunda2.registration_process.dto;

/**
 * DTO for task type to group mapping
 */
public class TaskGroupMapping {
    private String taskType;
    private String groupName;

    public TaskGroupMapping() {
    }

    public TaskGroupMapping(String taskType, String groupName) {
        this.taskType = taskType;
        this.groupName = groupName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}