package com.clinicnexus.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.model.Appointment;

/**
 * Data Access Object for Appointment operations
 */
public class AppointmentDAO {
    
    /**
     * Add new appointment
     * @param appointment Appointment object to add
     * @return Generated appointment ID or -1 if failed
     */
    public int addAppointment(Appointment appointment) {
        String sql = "INSERT INTO Appointment (PatientID, DoctorID, Date, Time, Duration, VisitType, Status, Notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, appointment.getPatientID());
            pstmt.setInt(2, appointment.getDoctorID());
            pstmt.setDate(3, appointment.getDate());
            pstmt.setTime(4, appointment.getTime());
            pstmt.setInt(5, appointment.getDuration());
            pstmt.setString(6, appointment.getVisitType());
            pstmt.setString(7, appointment.getStatus());
            pstmt.setString(8, appointment.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding appointment: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Get appointment by ID
     * @param appointmentID ID of appointment to retrieve
     * @return Appointment object or null if not found
     */
    public Appointment getAppointment(int appointmentID) {
        String sql = "SELECT * FROM Appointment WHERE AppointmentID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Appointment(
                    rs.getInt("AppointmentID"),
                    rs.getInt("PatientID"),
                    rs.getInt("DoctorID"),
                    rs.getDate("Date"),
                    rs.getTime("Time"),
                    rs.getInt("Duration"),
                    rs.getString("VisitType"),
                    rs.getString("Status"),
                    rs.getString("Notes")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update appointment
     * @param appointment Appointment object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE Appointment SET PatientID = ?, DoctorID = ?, Date = ?, Time = ?, Duration = ?, VisitType = ?, Status = ?, Notes = ? WHERE AppointmentID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointment.getPatientID());
            pstmt.setInt(2, appointment.getDoctorID());
            pstmt.setDate(3, appointment.getDate());
            pstmt.setTime(4, appointment.getTime());
            pstmt.setInt(5, appointment.getDuration());
            pstmt.setString(6, appointment.getVisitType());
            pstmt.setString(7, appointment.getStatus());
            pstmt.setString(8, appointment.getNotes());
            pstmt.setInt(9, appointment.getAppointmentID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete appointment and all related records
     * @param appointmentID ID of appointment to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteAppointment(int appointmentID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete related records first (in order of dependencies)
            // 1. Delete appointment inventory records
            String deleteAppointmentInventorySql = "DELETE FROM Appointment_Inventory WHERE AppointmentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentInventorySql)) {
                pstmt.setInt(1, appointmentID);
                pstmt.executeUpdate();
            }
            
            // 2. Delete billing records
            String deleteBillingSql = "DELETE FROM Billing WHERE AppointmentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBillingSql)) {
                pstmt.setInt(1, appointmentID);
                pstmt.executeUpdate();
            }
            
            // 3. Delete feedback records
            String deleteFeedbackSql = "DELETE FROM Feedback WHERE AppointmentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFeedbackSql)) {
                pstmt.setInt(1, appointmentID);
                pstmt.executeUpdate();
            }
            
            // 4. Finally delete the appointment
            String deleteAppointmentSql = "DELETE FROM Appointment WHERE AppointmentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentSql)) {
                pstmt.setInt(1, appointmentID);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                } else {
                    conn.rollback(); // Rollback if no appointment was deleted
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get list of appointments with optional filtering
     * @param dateFilter Filter by date (null for no filter)
     * @param doctorID Filter by doctor ID (null for no filter)
     * @param statusFilter Filter by status (null for no filter)
     * @param visitTypeFilter Filter by visit type (null for no filter)
     * @return List of Appointment objects
     */
    public List<Appointment> getAppointmentList(Date dateFilter, Integer doctorID, String statusFilter, String visitTypeFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Appointment WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (dateFilter != null) {
            sql.append(" AND Date = ?");
            params.add(dateFilter);
        }
        
        if (doctorID != null) {
            sql.append(" AND DoctorID = ?");
            params.add(doctorID);
        }
        
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append(" AND Status = ?");
            params.add(statusFilter);
        }
        
        if (visitTypeFilter != null && !visitTypeFilter.trim().isEmpty()) {
            sql.append(" AND VisitType = ?");
            params.add(visitTypeFilter);
        }
        
        sql.append(" ORDER BY Date DESC, Time DESC");
        
        List<Appointment> appointmentList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                appointmentList.add(new Appointment(
                    rs.getInt("AppointmentID"),
                    rs.getInt("PatientID"),
                    rs.getInt("DoctorID"),
                    rs.getDate("Date"),
                    rs.getTime("Time"),
                    rs.getInt("Duration"),
                    rs.getString("VisitType"),
                    rs.getString("Status"),
                    rs.getString("Notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment list: " + e.getMessage());
        }
        
        return appointmentList;
    }
    
    /**
     * Get appointment with related patient and doctor records
     * @param appointmentID ID of appointment
     * @return Map containing appointment info and related records
     */
    public Map<String, Object> getAppointmentWithRelatedRecords(int appointmentID) {
        Map<String, Object> result = new HashMap<>();
        
        String sql = "SELECT a.*, " +
                    "p.Name as PatientName, p.Phone as PatientPhone, p.Email as PatientEmail, " +
                    "s.Name as DoctorName, s.Specialization as DoctorSpecialization, s.Phone as DoctorPhone " +
                    "FROM Appointment a " +
                    "JOIN Patient p ON a.PatientID = p.PatientID " +
                    "JOIN Staff s ON a.DoctorID = s.StaffID " +
                    "WHERE a.AppointmentID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Appointment details
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("appointmentID", rs.getInt("AppointmentID"));
                appointment.put("date", rs.getDate("Date"));
                appointment.put("time", rs.getTime("Time"));
                appointment.put("duration", rs.getInt("Duration"));
                appointment.put("visitType", rs.getString("VisitType"));
                appointment.put("status", rs.getString("Status"));
                appointment.put("notes", rs.getString("Notes"));
                
                // Patient details
                Map<String, Object> patient = new HashMap<>();
                patient.put("patientID", rs.getInt("PatientID"));
                patient.put("name", rs.getString("PatientName"));
                patient.put("phone", rs.getString("PatientPhone"));
                patient.put("email", rs.getString("PatientEmail"));
                
                // Doctor details
                Map<String, Object> doctor = new HashMap<>();
                doctor.put("doctorID", rs.getInt("DoctorID"));
                doctor.put("name", rs.getString("DoctorName"));
                doctor.put("specialization", rs.getString("DoctorSpecialization"));
                doctor.put("phone", rs.getString("DoctorPhone"));
                
                result.put("appointment", appointment);
                result.put("patient", patient);
                result.put("doctor", doctor);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment with related records: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Check if doctor has conflicting appointments
     * @param doctorID ID of doctor
     * @param date Date of appointment
     * @param time Time of appointment
     * @param excludeAppointmentID Appointment ID to exclude from check (for updates)
     * @return true if conflict exists, false otherwise
     */
    public boolean hasAppointmentConflict(int doctorID, Date date, Time time, Integer excludeAppointmentID) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Appointment WHERE DoctorID = ? AND Date = ? AND Time = ? AND Status != 'Canceled'");
        
        if (excludeAppointmentID != null) {
            sql.append(" AND AppointmentID != ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setInt(1, doctorID);
            pstmt.setDate(2, date);
            pstmt.setTime(3, time);
            
            if (excludeAppointmentID != null) {
                pstmt.setInt(4, excludeAppointmentID);
            }
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking appointment conflict: " + e.getMessage());
            return true; // Assume conflict to be safe
        }
    }
    
    /**
     * Get appointments for a specific date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of appointments within the date range
     */
    public List<Appointment> getAppointmentsByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT * FROM Appointment WHERE Date BETWEEN ? AND ? ORDER BY Date, Time";
        List<Appointment> appointments = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("AppointmentID"),
                    rs.getInt("PatientID"),
                    rs.getInt("DoctorID"),
                    rs.getDate("Date"),
                    rs.getTime("Time"),
                    rs.getInt("Duration"),
                    rs.getString("VisitType"),
                    rs.getString("Status"),
                    rs.getString("Notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments by date range: " + e.getMessage());
        }
        
        return appointments;
    }
    
    /**
     * Update appointment status
     * @param appointmentID ID of appointment
     * @param newStatus New status
     * @return true if successful, false otherwise
     */
    public boolean updateAppointmentStatus(int appointmentID, String newStatus) {
        String sql = "UPDATE Appointment SET Status = ? WHERE AppointmentID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentID);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get appointments with patient and doctor names
     * @return List of appointments with names
     */
    public List<Map<String, Object>> getAppointmentsWithNames() {
        String sql = "SELECT a.*, p.Name as PatientName, s.Name as DoctorName " +
                    "FROM Appointment a " +
                    "JOIN Patient p ON a.PatientID = p.PatientID " +
                    "JOIN Staff s ON a.DoctorID = s.StaffID " +
                    "ORDER BY a.Date DESC, a.Time DESC";
        
        List<Map<String, Object>> appointments = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("appointmentID", rs.getInt("AppointmentID"));
                appointment.put("patientName", rs.getString("PatientName"));
                appointment.put("doctorName", rs.getString("DoctorName"));
                appointment.put("date", rs.getDate("Date"));
                appointment.put("time", rs.getTime("Time"));
                appointment.put("duration", rs.getInt("Duration"));
                appointment.put("visitType", rs.getString("VisitType"));
                appointment.put("status", rs.getString("Status"));
                appointment.put("notes", rs.getString("Notes"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments with names: " + e.getMessage());
        }
        
        return appointments;
    }
    
    /**
     * Add appointment using patient and doctor names
     * @param patientName Name of patient
     * @param doctorName Name of doctor
     * @param date Date of appointment
     * @param time Time of appointment
     * @param duration Duration in minutes
     * @param visitType Type of visit
     * @param status Status of appointment
     * @param notes Notes
     * @return Generated appointment ID or -1 if failed
     */
    public int addAppointmentWithNames(String patientName, String doctorName, Date date, 
                                      Time time, Integer duration, String visitType, 
                                      String status, String notes) {
        // First get patient and doctor IDs directly using SQL queries
        int patientID = getPatientIdByName(patientName);
        int doctorID = getStaffIdByName(doctorName);
        
        if (patientID == -1 || doctorID == -1) {
            System.err.println("Patient or doctor not found. Patient: " + patientName + ", Doctor: " + doctorName);
            return -1;
        }
        
        // Create appointment object
        Appointment appointment = new Appointment();
        appointment.setPatientID(patientID);
        appointment.setDoctorID(doctorID);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setDuration(duration);
        appointment.setVisitType(visitType);
        appointment.setStatus(status);
        appointment.setNotes(notes);
        
        return addAppointment(appointment);
    }
    
    /**
     * Get patient ID by name
     * @param name Name of patient
     * @return Patient ID or -1 if not found
     */
    private int getPatientIdByName(String name) {
        String sql = "SELECT PatientID FROM Patient WHERE Name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("PatientID");
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient ID by name: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Get staff ID by name
     * @param name Name of staff member
     * @return Staff ID or -1 if not found
     */
    private int getStaffIdByName(String name) {
        String sql = "SELECT StaffID FROM Staff WHERE Name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("StaffID");
            }
        } catch (SQLException e) {
            System.err.println("Error getting staff ID by name: " + e.getMessage());
        }
        return -1;
    }
}