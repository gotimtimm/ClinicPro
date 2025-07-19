package com.clinicnexus.util;

/**
 * Result class for visit processing operations
 */
public class VisitResult {
    private final boolean success;
    private final String message;
    
    public VisitResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}