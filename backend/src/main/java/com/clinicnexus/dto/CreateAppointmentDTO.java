package com.clinicnexus.dto;

public class CreateAppointmentDTO {
    private String patientName;
    private String doctorName;
    private String date;
    private String time;
    private Integer duration;
    private String visitType;
    private String status;
    private String notes;

    // Constructors
    public CreateAppointmentDTO() {}

    public CreateAppointmentDTO(String patientName, String doctorName, String date, 
                               String time, Integer duration, String visitType, 
                               String status, String notes) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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