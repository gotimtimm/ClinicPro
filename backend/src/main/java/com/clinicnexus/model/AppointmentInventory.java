package com.clinicnexus.model;

/**
 * AppointmentInventory model class representing inventory items used in appointments
 */
public class AppointmentInventory {
    private int appointmentID;
    private int itemID;
    private int quantityUsed;
    
    /**
     * Default constructor
     */
    public AppointmentInventory() {}
    
    /**
     * Constructor with all fields
     */
    public AppointmentInventory(int appointmentID, int itemID, int quantityUsed) {
        this.appointmentID = appointmentID;
        this.itemID = itemID;
        this.quantityUsed = quantityUsed;
    }
    
    // Getters and Setters
    public int getAppointmentID() { return appointmentID; }
    public void setAppointmentID(int appointmentID) { this.appointmentID = appointmentID; }
    
    public int getItemID() { return itemID; }
    public void setItemID(int itemID) { this.itemID = itemID; }
    
    public int getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(int quantityUsed) { this.quantityUsed = quantityUsed; }
    
    @Override
    public String toString() {
        return "AppointmentInventory{" +
                "appointmentID=" + appointmentID +
                ", itemID=" + itemID +
                ", quantityUsed=" + quantityUsed +
                '}';
    }
}