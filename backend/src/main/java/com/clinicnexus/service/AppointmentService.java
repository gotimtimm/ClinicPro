package com.clinicnexus.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.dao.AppointmentDAO;
import com.clinicnexus.model.Appointment;

@Service
public class AppointmentService {
    private final AppointmentDAO appointmentDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
    }

    public int createAppointment(Appointment appointment) {
        return appointmentDAO.addAppointment(appointment);
    }

    public Appointment getAppointment(int appointmentID) {
        return appointmentDAO.getAppointment(appointmentID);
    }

    public boolean updateAppointment(Appointment appointment) {
        return appointmentDAO.updateAppointment(appointment);
    }

    public boolean deleteAppointment(int appointmentID) {
        return appointmentDAO.deleteAppointment(appointmentID);
    }

    public List<Appointment> listAppointments(Date dateFilter, Integer doctorID, String statusFilter, String visitTypeFilter) {
        return appointmentDAO.getAppointmentList(dateFilter, doctorID, statusFilter, visitTypeFilter);
    }

    public Map<String, Object> getAppointmentWithRelatedRecords(int appointmentID) {
        return appointmentDAO.getAppointmentWithRelatedRecords(appointmentID);
    }
    
    public List<Map<String, Object>> getAppointmentsWithNames() {
        return appointmentDAO.getAppointmentsWithNames();
    }
    
    public int createAppointmentWithNames(String patientName, String doctorName, String date, 
                                         String time, Integer duration, String visitType, 
                                         String status, String notes) {
        try {
            // Validate required fields
            if (patientName == null || patientName.trim().isEmpty()) {
                System.err.println("Patient name is required");
                return -1;
            }
            if (doctorName == null || doctorName.trim().isEmpty()) {
                System.err.println("Doctor name is required");
                return -1;
            }
            if (date == null || date.trim().isEmpty()) {
                System.err.println("Date is required");
                return -1;
            }
            if (time == null || time.trim().isEmpty()) {
                System.err.println("Time is required");
                return -1;
            }
            if (duration == null || duration <= 0) {
                System.err.println("Duration must be greater than 0");
                return -1;
            }
            if (visitType == null || visitType.trim().isEmpty()) {
                System.err.println("Visit type is required");
                return -1;
            }
            if (status == null || status.trim().isEmpty()) {
                System.err.println("Status is required");
                return -1;
            }
            
            // Convert string date and time to Date and Time objects
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            
            // Handle time format - add seconds if not present
            String timeWithSeconds = time;
            if (time.split(":").length == 2) {
                timeWithSeconds = time + ":00";
            }
            java.sql.Time sqlTime = java.sql.Time.valueOf(timeWithSeconds);
            
            return appointmentDAO.addAppointmentWithNames(patientName, doctorName, sqlDate, sqlTime, duration, visitType, status, notes);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid date or time format: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("Error creating appointment with names: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
} 
