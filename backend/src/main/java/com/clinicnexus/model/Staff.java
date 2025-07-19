package com.clinicnexus.model;

import java.sql.Date;

/**
 * Staff model class representing staff members in the clinic
 */
public class Staff {
    private int staffID;
    private String name;
    private String jobType;
    private String specialization;
    private String licenseNumber;
    private String phone;
    private String email;
    private Date hireDate;
    private String workingDays;
    private boolean activeStatus;
    
    /**
     * Default constructor
     */
    public Staff() {}
    
    /**
     * Constructor with all fields
     */
    public Staff(int staffID, String name, String jobType, String specialization, 
                String licenseNumber, String phone, String email, Date hireDate, 
                String workingDays, boolean activeStatus) {
        this.staffID = staffID;
        this.name = name;
        this.jobType = jobType;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.email = email;
        this.hireDate = hireDate;
        this.workingDays = workingDays;
        this.activeStatus = activeStatus;
    }
    
    // Getters and Setters
    public int getStaffID() { return staffID; }
    public void setStaffID(int staffID) { this.staffID = staffID; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
    
    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    
    public boolean isActiveStatus() { return activeStatus; }
    public void setActiveStatus(boolean activeStatus) { this.activeStatus = activeStatus; }
    
    @Override
    public String toString() {
        return "Staff{" +
                "staffID=" + staffID +
                ", name='" + name + '\'' +
                ", jobType='" + jobType + '\'' +
                ", specialization='" + specialization + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", activeStatus=" + activeStatus +
                '}';
    }
}