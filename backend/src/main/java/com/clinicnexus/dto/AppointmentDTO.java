package com.clinicnexus.dto;

import java.sql.Date;
import java.sql.Time;

public class AppointmentDTO {
    private Integer appointmentID;
    private String patientName;
    private String doctorName;
    private Date date;
    private Time time;
    private Integer duration;
    private String visitType;
    private String status;
    private String notes;

    // Constructors
    public AppointmentDTO() {}

    public AppointmentDTO(Integer appointmentID, String patientName, String doctorName, 
                         Date date, Time time, Integer duration, String visitType, 
                         String status, String notes) {
        this.appointmentID = appointmentID;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.visitType = visitType;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(Integer appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 