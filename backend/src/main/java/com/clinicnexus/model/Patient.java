package com.clinicnexus.model;

import java.sql.Date;

/**
 * Patient model class representing patients in the clinic
 */
public class Patient {
    private int patientID;
    private String name;
    private Date birthDate;
    private String phone;
    private String email;
    private String insuranceInfo;
    private Date firstVisitDate;
    private int primaryDoctorID;
    private boolean activeStatus;
    
    /**
     * Default constructor
     */
    public Patient() {}
    
    /**
     * Constructor with all fields
     */
    public Patient(int patientID, String name, Date birthDate, String phone, 
                  String email, String insuranceInfo, Date firstVisitDate, 
                  int primaryDoctorID, boolean activeStatus) {
        this.patientID = patientID;
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
        this.insuranceInfo = insuranceInfo;
        this.firstVisitDate = firstVisitDate;
        this.primaryDoctorID = primaryDoctorID;
        this.activeStatus = activeStatus;
    }
    
    // Getters and Setters
    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getInsuranceInfo() { return insuranceInfo; }
    public void setInsuranceInfo(String insuranceInfo) { this.insuranceInfo = insuranceInfo; }
    
    public Date getFirstVisitDate() { return firstVisitDate; }
    public void setFirstVisitDate(Date firstVisitDate) { this.firstVisitDate = firstVisitDate; }
    
    public int getPrimaryDoctorID() { return primaryDoctorID; }
    public void setPrimaryDoctorID(int primaryDoctorID) { this.primaryDoctorID = primaryDoctorID; }
    
    public boolean isActiveStatus() { return activeStatus; }
    public void setActiveStatus(boolean activeStatus) { this.activeStatus = activeStatus; }
    
    @Override
    public String toString() {
        return "Patient{" +
                "patientID=" + patientID +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", insuranceInfo='" + insuranceInfo + '\'' +
                ", firstVisitDate=" + firstVisitDate +
                ", primaryDoctorID=" + primaryDoctorID +
                ", activeStatus=" + activeStatus +
                '}';
    }
}