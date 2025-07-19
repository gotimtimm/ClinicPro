package com.clinicnexus.util;

public class RestockResult {
    private final boolean success;
    private final String message;
    
    public RestockResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}