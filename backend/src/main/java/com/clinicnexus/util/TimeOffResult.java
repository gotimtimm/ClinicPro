package com.clinicnexus.util;

public class TimeOffResult {
    private final boolean success;
    private final String message;
    private final String status;
    
    public TimeOffResult(boolean success, String message, String status) {
        this.success = success;
        this.message = message;
        this.status = status;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
}