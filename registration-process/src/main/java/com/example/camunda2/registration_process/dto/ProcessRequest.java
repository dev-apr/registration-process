package com.example.camunda2.registration_process.dto;

public class ProcessRequest {

    private int wage;
    private String requestDate;

    public int getWage() {
        return wage;
    }

    public void setWage(int wage) {
        this.wage = wage;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
    
    // Keep the old setter for backward compatibility
    public void setDate(String requestDate) {
        this.requestDate = requestDate;
    }
}
