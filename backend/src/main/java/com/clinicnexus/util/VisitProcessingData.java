package com.clinicnexus.util;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

/**
 * Data class for visit processing
 */
public class VisitProcessingData {
    private int appointmentID;
    private Map<String, String> vitalSigns;
    private String diagnosis;   
    private String treatment;
    private Map<Integer, Integer> inventoryUsage; // ItemID -> Quantity
    private double baseAmount;
    private boolean scheduleFollowUp;
    private Date followUpDate;
    private Time followUpTime;
    
    // Constructors
    public VisitProcessingData() {
        this.vitalSigns = new HashMap<>();
        this.inventoryUsage = new HashMap<>();
    }
    
    public VisitProcessingData(int appointmentID) {
        this.appointmentID = appointmentID;
        this.vitalSigns = new HashMap<>();
        this.inventoryUsage = new HashMap<>();
    }
    
    // Getters and Setters
    public int getAppointmentID() { return appointmentID; }
    public void setAppointmentID(int appointmentID) { this.appointmentID = appointmentID; }
    
    public Map<String, String> getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(Map<String, String> vitalSigns) { this.vitalSigns = vitalSigns; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    
    public Map<Integer, Integer> getInventoryUsage() { return inventoryUsage; }
    public void setInventoryUsage(Map<Integer, Integer> inventoryUsage) { this.inventoryUsage = inventoryUsage; }
    
    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }
    
    public boolean isScheduleFollowUp() { return scheduleFollowUp; }
    public void setScheduleFollowUp(boolean scheduleFollowUp) { this.scheduleFollowUp = scheduleFollowUp; }
    
    public Date getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(Date followUpDate) { this.followUpDate = followUpDate; }
    
    public Time getFollowUpTime() { return followUpTime; }
    public void setFollowUpTime(Time followUpTime) { this.followUpTime = followUpTime; }
    
    // Utility methods
    public void addVitalSign(String metric, String value) {
        vitalSigns.put(metric, value);
    }
    
    public void addInventoryUsage(int itemID, int quantity) {
        inventoryUsage.put(itemID, quantity);
    }
}