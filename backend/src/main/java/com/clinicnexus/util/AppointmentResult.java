package com.clinicnexus.util;

/**
 * Result class for appointment operations
 */
public class AppointmentResult {
    private final boolean success;
    private final int appointmentID;
    private final String message;
    
    public AppointmentResult(boolean success, int appointmentID, String message) {
        this.success = success;
        this.message = message;
        this.appointmentID = appointmentID;
    }
    
    public boolean isSuccess() { return success; }
    public int getAppointmentID() { return appointmentID; }
    public String getMessage() { return message; }
}

