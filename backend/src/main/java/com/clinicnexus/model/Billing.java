package com.clinicnexus.model;

import java.sql.Date;

/**
 * Billing model class representing billing records for appointments
 */
public class Billing {
    private int billingID;
    private int appointmentID;
    private double amount;
    private boolean paid;
    private Date paymentDate;
    
    /**
     * Default constructor
     */
    public Billing() {}
    
    /**
     * Constructor with all fields
     */
    public Billing(int billingID, int appointmentID, double amount, boolean paid, Date paymentDate) {
        this.billingID = billingID;
        this.appointmentID = appointmentID;
        this.amount = amount;
        this.paid = paid;
        this.paymentDate = paymentDate;
    }
    
    // Getters and Setters
    public int getBillingID() { return billingID; }
    public void setBillingID(int billingID) { this.billingID = billingID; }
    
    public int getAppointmentID() { return appointmentID; }
    public void setAppointmentID(int appointmentID) { this.appointmentID = appointmentID; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    
    /**
     * Check if billing is overdue (unpaid)
     * @return true if not paid
     */
    public boolean isOverdue() {
        return !paid;
    }
    
    @Override
    public String toString() {
        return "Billing{" +
                "billingID=" + billingID +
                ", appointmentID=" + appointmentID +
                ", amount=" + amount +
                ", paid=" + paid +
                ", paymentDate=" + paymentDate +
                '}';
    }
}