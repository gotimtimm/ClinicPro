package com.clinicnexus.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.dao.AppointmentDAO;
import com.clinicnexus.dao.PatientDAO;
import com.clinicnexus.dao.StaffDAO;
import com.clinicnexus.model.Appointment;
import com.clinicnexus.util.AppointmentResult;

/**
 * Transaction service for appointment scheduling operations
 * Handles Transaction 4.1: Appointment Scheduling
 */
@Service
public class AppointmentTransactionService {
    
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private StaffDAO staffDAO;
    
    public AppointmentTransactionService() {
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.staffDAO = new StaffDAO();
    }
    
    /**
     * Schedule a new appointment with full validation
     * @param appointment Appointment object with details
     * @return AppointmentResult with success status and appointment ID
     */
    public AppointmentResult scheduleAppointment(Appointment appointment) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Check doctor availability
            if (!isDoctorAvailable(conn, appointment.getDoctorID(), appointment.getDate(), appointment.getTime())) {
                throw new SQLException("Doctor is not available at the requested time");
            }
            
            // Step 2: Validate patient exists and is active
            if (!isPatientValid(conn, appointment.getPatientID())) {
                throw new SQLException("Patient does not exist or is inactive");
            }
            
            // Step 3: Validate doctor exists and is active
            if (!isDoctorValid(conn, appointment.getDoctorID())) {
                throw new SQLException("Doctor does not exist or is inactive");
            }
            
            // Step 4: Create appointment record
            int appointmentID = createAppointmentRecord(conn, appointment);
            if (appointmentID == -1) {
                throw new SQLException("Failed to create appointment record");
            }
            
            // Step 5: Create initial billing record
            createInitialBillingRecord(conn, appointmentID, appointment.getVisitType());
            
            // Step 6: Update doctor calendar (mark timeslot as booked)
            updateDoctorCalendar(conn, appointment.getDoctorID(), appointment.getDate(), appointment.getTime());
            
            // Step 7: Send notification to patient
            sendPatientNotification(conn, appointment.getPatientID(), appointmentID, appointment);
            
            conn.commit();
            return new AppointmentResult(true, appointmentID, "Appointment scheduled successfully");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error scheduling appointment: " + e.getMessage());
            return new AppointmentResult(false, -1, e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check if doctor is available at specified time
     * @param conn Database connection
     * @param doctorID Doctor ID
     * @param date Appointment date
     * @param time Appointment time
     * @return true if available, false otherwise
     */
    private boolean isDoctorAvailable(Connection conn, int doctorID, Date date, Time time) throws SQLException {
        // Check for existing appointments at the same time
        String sql = "SELECT COUNT(*) FROM Appointment WHERE DoctorID = ? AND Date = ? AND Time = ? AND Status != 'Canceled'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorID);
            pstmt.setDate(2, date);
            pstmt.setTime(3, time);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Doctor has existing appointment
            }
        }
        
        // Check if doctor is working on this day
        String workingDaysSql = "SELECT WorkingDays FROM Staff WHERE StaffID = ? AND JobType = 'Doctor' AND ActiveStatus = true";
        try (PreparedStatement pstmt = conn.prepareStatement(workingDaysSql)) {
            pstmt.setInt(1, doctorID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String workingDays = rs.getString("WorkingDays");
                // Simple check - in real implementation, you'd parse working days and check against appointment date
                return workingDays != null && !workingDays.trim().isEmpty();
            }
        }
        
        return false;
    }
    
    /**
     * Validate if patient exists and is active
     */
    private boolean isPatientValid(Connection conn, int patientID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Patient WHERE PatientID = ? AND ActiveStatus = true";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    /**
     * Validate if doctor exists and is active
     */
    private boolean isDoctorValid(Connection conn, int doctorID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Staff WHERE StaffID = ? AND JobType = 'Doctor' AND ActiveStatus = true";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    /**
     * Create appointment record in database
     */
    private int createAppointmentRecord(Connection conn, Appointment appointment) throws SQLException {
        String sql = "INSERT INTO Appointment (PatientID, DoctorID, Date, Time, Duration, VisitType, Status, Notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, appointment.getPatientID());
            pstmt.setInt(2, appointment.getDoctorID());
            pstmt.setDate(3, appointment.getDate());
            pstmt.setTime(4, appointment.getTime());
            pstmt.setInt(5, appointment.getDuration() > 0 ? appointment.getDuration() : 30); // Default 30 minutes
            pstmt.setString(6, appointment.getVisitType());
            pstmt.setString(7, "Not Done"); // Default status
            pstmt.setString(8, appointment.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }
    
    /**
     * Create initial billing record for the appointment
     */
    private void createInitialBillingRecord(Connection conn, int appointmentID, String visitType) throws SQLException {
        double amount = calculateAppointmentFee(visitType);
        String sql = "INSERT INTO Billing (AppointmentID, Amount, Paid) VALUES (?, ?, false)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentID);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Calculate appointment fee based on visit type
     */
    private double calculateAppointmentFee(String visitType) {
        switch (visitType) {
            case "Check-up": return 500.0;
            case "Procedure": return 1500.0;
            case "Emergency": return 2000.0;
            default: return 500.0;
        }
    }
    
    /**
     * Update doctor calendar (placeholder for calendar management)
     */
    private void updateDoctorCalendar(Connection conn, int doctorID, Date date, Time time) throws SQLException {
        // In a real implementation, this would update a calendar/schedule table
        // For now, we'll just log the booking
        System.out.println("Doctor " + doctorID + " calendar updated for " + date + " at " + time);
    }
    
    /**
     * Send notification to patient (placeholder for notification system)
     */
    private void sendPatientNotification(Connection conn, int patientID, int appointmentID, Appointment appointment) throws SQLException {
        // Get patient contact info
        String sql = "SELECT Name, Phone, Email FROM Patient WHERE PatientID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String patientName = rs.getString("Name");
                String phone = rs.getString("Phone");
                String email = rs.getString("Email");
                
                // Placeholder for actual notification sending
                System.out.println("Notification sent to " + patientName + " (" + email + "):");
                System.out.println("Appointment ID: " + appointmentID);
                System.out.println("Date: " + appointment.getDate() + " at " + appointment.getTime());
                System.out.println("Visit Type: " + appointment.getVisitType());
            }
        }
    }
    
    /**
     * Get available time slots for a doctor on a specific date
     */
    public List<String> getAvailableTimeSlots(int doctorID, Date date) {
        List<String> availableSlots = new ArrayList<>();
        
        // Standard working hours (9 AM to 5 PM)
        String[] standardSlots = {
            "09:00:00", "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00",
            "13:00:00", "13:30:00", "14:00:00", "14:30:00", "15:00:00", "15:30:00", "16:00:00", "16:30:00"
        };
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT Time FROM Appointment WHERE DoctorID = ? AND Date = ? AND Status != 'Canceled'";
            Set<String> bookedSlots = new HashSet<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, doctorID);
                pstmt.setDate(2, date);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    bookedSlots.add(rs.getTime("Time").toString());
                }
            }
            
            // Filter out booked slots
            for (String slot : standardSlots) {
                if (!bookedSlots.contains(slot)) {
                    availableSlots.add(slot);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available time slots: " + e.getMessage());
        }
        
        return availableSlots;
    }
}
