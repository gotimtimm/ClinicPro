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
        // Convert string date and time to Date and Time objects
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        
        // Handle time format - add seconds if not present
        String timeWithSeconds = time;
        if (time.split(":").length == 2) {
            timeWithSeconds = time + ":00";
        }
        java.sql.Time sqlTime = java.sql.Time.valueOf(timeWithSeconds);
        
        return appointmentDAO.addAppointmentWithNames(patientName, doctorName, sqlDate, sqlTime, duration, visitType, status, notes);
    }
} 
