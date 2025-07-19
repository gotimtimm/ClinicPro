package com.clinicnexus.model;

import java.sql.Date;
import java.sql.Time;

/**
 * Appointment model class representing appointments in the clinic
 */
public class Appointment {
    private int appointmentID;
    private int patientID;
    private int doctorID;
    private Date date;
    private Time time;
    private int duration;
    private String visitType;
    private String status;
    private String notes;
    
    /**
     * Default constructor
     */
    public Appointment() {}
    
    /**
     * Constructor with all fields
     */
    public Appointment(int appointmentID, int patientID, int doctorID, Date date, 
                      Time time, int duration, String visitType, String status, String notes) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.visitType = visitType;
        this.status = status;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getAppointmentID() { return appointmentID; }
    public void setAppointmentID(int appointmentID) { this.appointmentID = appointmentID; }
    
    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }
    
    public int getDoctorID() { return doctorID; }
    public void setDoctorID(int doctorID) { this.doctorID = doctorID; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public Time getTime() { return time; }
    public void setTime(Time time) { this.time = time; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    /**
     * Check if appointment is completed
     * @return true if status is "Done"
     */
    public boolean isCompleted() {
        return "Done".equals(status);
    }
    
    /**
     * Check if appointment is canceled
     * @return true if status is "Canceled"
     */
    public boolean isCanceled() {
        return "Canceled".equals(status);
    }
    
    /**
     * Check if appointment is pending
     * @return true if status is "Not Done"
     */
    public boolean isPending() {
        return "Not Done".equals(status);
    }
    
    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentID=" + appointmentID +
                ", patientID=" + patientID +
                ", doctorID=" + doctorID +
                ", date=" + date +
                ", time=" + time +
                ", duration=" + duration +
                ", visitType='" + visitType + '\'' +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}