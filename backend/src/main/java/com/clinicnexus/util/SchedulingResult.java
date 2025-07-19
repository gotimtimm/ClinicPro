package com.clinicnexus.util;

/**
 * Result class for scheduling operations
 */
public class SchedulingResult {
    private final boolean success;
    private final String message;
    
    public SchedulingResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}