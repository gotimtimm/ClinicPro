package com.clinicnexus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.clinicnexus.model.Patient;

/**
 * Data Access Object for Patient operations
 */
@Repository
public class PatientDAO {
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * Add new patient
     * @param patient Patient object to add
     * @return true if successful, false otherwise
     */
    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO Patient (Name, BirthDate, Phone, Email, InsuranceInfo, FirstVisitDate, PrimaryDoctorID, ActiveStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patient.getName());
            pstmt.setDate(2, patient.getBirthDate());
            pstmt.setString(3, patient.getPhone());
            pstmt.setString(4, patient.getEmail());
            pstmt.setString(5, patient.getInsuranceInfo());
            pstmt.setDate(6, patient.getFirstVisitDate());
            pstmt.setInt(7, patient.getPrimaryDoctorID());
            pstmt.setBoolean(8, patient.isActiveStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get patient by ID
     * @param patientID ID of patient to retrieve
     * @return Patient object or null if not found
     */
    public Patient getPatient(int patientID) {
        String sql = "SELECT * FROM Patient WHERE PatientID = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Patient(
                    rs.getInt("PatientID"),
                    rs.getString("Name"),
                    rs.getDate("BirthDate"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getString("InsuranceInfo"),
                    rs.getDate("FirstVisitDate"),
                    rs.getInt("PrimaryDoctorID"),
                    rs.getBoolean("ActiveStatus")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patient: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update patient information
     * @param patient Patient object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE Patient SET Name = ?, BirthDate = ?, Phone = ?, Email = ?, InsuranceInfo = ?, FirstVisitDate = ?, PrimaryDoctorID = ?, ActiveStatus = ? WHERE PatientID = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patient.getName());
            pstmt.setDate(2, patient.getBirthDate());
            pstmt.setString(3, patient.getPhone());
            pstmt.setString(4, patient.getEmail());
            pstmt.setString(5, patient.getInsuranceInfo());
            pstmt.setDate(6, patient.getFirstVisitDate());
            pstmt.setInt(7, patient.getPrimaryDoctorID());
            pstmt.setBoolean(8, patient.isActiveStatus());
            pstmt.setInt(9, patient.getPatientID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete patient and all related records
     * @param patientID ID of patient to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePatient(int patientID) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete related records first (in order of dependencies)
            // 1. Delete feedback records
            String deleteFeedbackSql = "DELETE FROM Feedback WHERE PatientID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFeedbackSql)) {
                pstmt.setInt(1, patientID);
                pstmt.executeUpdate();
            }
            
            // 2. Delete appointment inventory records for this patient's appointments
            String deleteAppointmentInventorySql = "DELETE ai FROM Appointment_Inventory ai " +
                                                  "JOIN Appointment a ON ai.AppointmentID = a.AppointmentID " +
                                                  "WHERE a.PatientID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentInventorySql)) {
                pstmt.setInt(1, patientID);
                pstmt.executeUpdate();
            }
            
            // 3. Delete billing records for this patient's appointments
            String deleteBillingSql = "DELETE b FROM Billing b " +
                                     "JOIN Appointment a ON b.AppointmentID = a.AppointmentID " +
                                     "WHERE a.PatientID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBillingSql)) {
                pstmt.setInt(1, patientID);
                pstmt.executeUpdate();
            }
            
            // 4. Delete feedback records for this patient's appointments
            String deleteAppointmentFeedbackSql = "DELETE f FROM Feedback f " +
                                                 "JOIN Appointment a ON f.AppointmentID = a.AppointmentID " +
                                                 "WHERE a.PatientID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentFeedbackSql)) {
                pstmt.setInt(1, patientID);
                pstmt.executeUpdate();
            }
            
            // 5. Delete appointments
            String deleteAppointmentsSql = "DELETE FROM Appointment WHERE PatientID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentsSql)) {
                pstmt.setInt(1, patientID);
                pstmt.executeUpdate();
            }
            
            // 6. Finally delete the patient
            String deletePatientSql = "DELETE FROM Patient WHERE PatientID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deletePatientSql)) {
                pstmt.setInt(1, patientID);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                } else {
                    conn.rollback(); // Rollback if no patient was deleted
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
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
     * Get list of patients with optional filtering
     * @param nameFilter Filter by name (null for no filter)
     * @param insuranceFilter Filter by insurance info (null for no filter)
     * @param activeFilter Filter by active status (null for no filter)
     * @return List of Patient objects
     */
    public List<Patient> getPatientList(String nameFilter, String insuranceFilter, Boolean activeFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Patient WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            sql.append(" AND Name LIKE ?");
            params.add("%" + nameFilter + "%");
        }
        
        if (insuranceFilter != null && !insuranceFilter.trim().isEmpty()) {
            sql.append(" AND InsuranceInfo LIKE ?");
            params.add("%" + insuranceFilter + "%");
        }
        
        if (activeFilter != null) {
            sql.append(" AND ActiveStatus = ?");
            params.add(activeFilter);
        }
        
        sql.append(" ORDER BY Name");
        
        List<Patient> patientList = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                patientList.add(new Patient(
                    rs.getInt("PatientID"),
                    rs.getString("Name"),
                    rs.getDate("BirthDate"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getString("InsuranceInfo"),
                    rs.getDate("FirstVisitDate"),
                    rs.getInt("PrimaryDoctorID"),
                    rs.getBoolean("ActiveStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patient list: " + e.getMessage());
        }
        
        return patientList;
    }
    
    /**
     * Get patient with their appointments
     * @param patientID ID of patient
     * @return Map containing patient info and related appointments
     */
    public Map<String, Object> getPatientWithAppointments(int patientID) {
        Map<String, Object> result = new HashMap<>();
        
        Patient patient = getPatient(patientID);
        if (patient != null) {
            result.put("patient", patient);
            
            String sql = "SELECT a.*, s.Name as DoctorName FROM Appointment a " +
                        "JOIN Staff s ON a.DoctorID = s.StaffID " +
                        "WHERE a.PatientID = ? ORDER BY a.Date DESC, a.Time DESC";
            
            List<Map<String, Object>> appointments = new ArrayList<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, patientID);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentID", rs.getInt("AppointmentID"));
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
                System.err.println("Error retrieving patient appointments: " + e.getMessage());
            }
            
            result.put("appointments", appointments);
        }
        
        return result;
    }
    
    /**
     * Get all active patients
     * @return List of active Patient objects
     */
    public List<Patient> getActivePatients() {
        List<Patient> patientList = new ArrayList<>();
        return patientList;
    }
    
    /**
     * Search patients by name
     * @param name Name to search for
     * @return List of Patient objects matching the name
     */
    public List<Patient> searchPatientsByName(String name) {
        return getPatientList(name, null, null);
    }
    
    /**
     * Get patient ID by name
     * @param name Name of patient
     * @return Patient ID or -1 if not found
     */
    public int getPatientIdByName(String name) {
        String sql = "SELECT PatientID FROM Patient WHERE Name = ?";
        
        try (Connection conn = dataSource.getConnection();
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
}