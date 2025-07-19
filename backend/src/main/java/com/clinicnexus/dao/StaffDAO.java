package com.clinicnexus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.model.Staff;

/**
 * Data Access Object for Staff operations
 */
public class StaffDAO {
    
    /**
     * Add new staff member
     * @param staff Staff object to add
     * @return true if successful, false otherwise
     */
    public boolean addStaff(Staff staff) {
        String sql = "INSERT INTO Staff (Name, JobType, Specialization, LicenseNumber, Phone, Email, HireDate, WorkingDays, ActiveStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getJobType());
            pstmt.setString(3, staff.getSpecialization());
            pstmt.setString(4, staff.getLicenseNumber());
            pstmt.setString(5, staff.getPhone());
            pstmt.setString(6, staff.getEmail());
            pstmt.setDate(7, staff.getHireDate());
            pstmt.setString(8, staff.getWorkingDays());
            pstmt.setBoolean(9, staff.isActiveStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding staff: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get staff member by ID
     * @param staffID ID of staff member to retrieve
     * @return Staff object or null if not found
     */
    public Staff getStaff(int staffID) {
        String sql = "SELECT * FROM Staff WHERE StaffID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, staffID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Staff(
                    rs.getInt("StaffID"),
                    rs.getString("Name"),
                    rs.getString("JobType"),
                    rs.getString("Specialization"),
                    rs.getString("LicenseNumber"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getDate("HireDate"),
                    rs.getString("WorkingDays"),
                    rs.getBoolean("ActiveStatus")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving staff: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update staff member
     * @param staff Staff object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE Staff SET Name = ?, JobType = ?, Specialization = ?, LicenseNumber = ?, Phone = ?, Email = ?, HireDate = ?, WorkingDays = ?, ActiveStatus = ? WHERE StaffID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getJobType());
            pstmt.setString(3, staff.getSpecialization());
            pstmt.setString(4, staff.getLicenseNumber());
            pstmt.setString(5, staff.getPhone());
            pstmt.setString(6, staff.getEmail());
            pstmt.setDate(7, staff.getHireDate());
            pstmt.setString(8, staff.getWorkingDays());
            pstmt.setBoolean(9, staff.isActiveStatus());
            pstmt.setInt(10, staff.getStaffID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating staff: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete staff member and all related records
     * @param staffID ID of staff member to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteStaff(int staffID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete related records first (in order of dependencies)
            // 1. Delete feedback records where staff is the doctor
            String deleteFeedbackSql = "DELETE FROM Feedback WHERE DoctorID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFeedbackSql)) {
                pstmt.setInt(1, staffID);
                pstmt.executeUpdate();
            }
            
            // 2. Delete appointment inventory records for this staff's appointments
            String deleteAppointmentInventorySql = "DELETE ai FROM Appointment_Inventory ai " +
                                                  "JOIN Appointment a ON ai.AppointmentID = a.AppointmentID " +
                                                  "WHERE a.DoctorID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentInventorySql)) {
                pstmt.setInt(1, staffID);
                pstmt.executeUpdate();
            }
            
            // 3. Delete billing records for this staff's appointments
            String deleteBillingSql = "DELETE b FROM Billing b " +
                                     "JOIN Appointment a ON b.AppointmentID = a.AppointmentID " +
                                     "WHERE a.DoctorID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBillingSql)) {
                pstmt.setInt(1, staffID);
                pstmt.executeUpdate();
            }
            
            // 4. Delete feedback records for this staff's appointments
            String deleteAppointmentFeedbackSql = "DELETE f FROM Feedback f " +
                                                 "JOIN Appointment a ON f.AppointmentID = a.AppointmentID " +
                                                 "WHERE a.DoctorID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentFeedbackSql)) {
                pstmt.setInt(1, staffID);
                pstmt.executeUpdate();
            }
            
            // 5. Delete appointments where staff is the doctor
            String deleteAppointmentsSql = "DELETE FROM Appointment WHERE DoctorID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentsSql)) {
                pstmt.setInt(1, staffID);
                pstmt.executeUpdate();
            }
            
            // 6. Update patients to remove this staff as primary doctor
            String updatePatientsSql = "UPDATE Patient SET PrimaryDoctorID = NULL WHERE PrimaryDoctorID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updatePatientsSql)) {
                pstmt.setInt(1, staffID);
                pstmt.executeUpdate();
            }
            
            // 7. Finally delete the staff member
            String deleteStaffSql = "DELETE FROM Staff WHERE StaffID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteStaffSql)) {
                pstmt.setInt(1, staffID);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                } else {
                    conn.rollback(); // Rollback if no staff was deleted
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting staff: " + e.getMessage());
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
     * Get list of staff members with optional filtering
     * @param jobTypeFilter Filter by job type (null for no filter)
     * @param specializationFilter Filter by specialization (null for no filter)
     * @param activeFilter Filter by active status (null for no filter)
     * @return List of Staff objects
     */
    public List<Staff> getStaffList(String jobTypeFilter, String specializationFilter, Boolean activeFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Staff WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (jobTypeFilter != null && !jobTypeFilter.trim().isEmpty()) {
            sql.append(" AND JobType = ?");
            params.add(jobTypeFilter);
        }
        
        if (specializationFilter != null && !specializationFilter.trim().isEmpty()) {
            sql.append(" AND Specialization LIKE ?");
            params.add("%" + specializationFilter + "%");
        }
        
        if (activeFilter != null) {
            sql.append(" AND ActiveStatus = ?");
            params.add(activeFilter);
        }
        
        sql.append(" ORDER BY Name");
        
        List<Staff> staffList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                staffList.add(new Staff(
                    rs.getInt("StaffID"),
                    rs.getString("Name"),
                    rs.getString("JobType"),
                    rs.getString("Specialization"),
                    rs.getString("LicenseNumber"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getDate("HireDate"),
                    rs.getString("WorkingDays"),
                    rs.getBoolean("ActiveStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving staff list: " + e.getMessage());
        }
        
        return staffList;
    }
    
    /**
     * Get staff member with their appointments
     * @param staffID ID of staff member
     * @return Map containing staff info and related appointments
     */
    public Map<String, Object> getStaffWithAppointments(int staffID) {
        Map<String, Object> result = new HashMap<>();
        
        Staff staff = getStaff(staffID);
        if (staff != null) {
            result.put("staff", staff);
            
            String sql = "SELECT a.*, p.Name as PatientName FROM Appointment a " +
                        "JOIN Patient p ON a.PatientID = p.PatientID " +
                        "WHERE a.DoctorID = ? ORDER BY a.Date DESC, a.Time DESC";
            
            List<Map<String, Object>> appointments = new ArrayList<>();
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, staffID);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentID", rs.getInt("AppointmentID"));
                    appointment.put("patientName", rs.getString("PatientName"));
                    appointment.put("date", rs.getDate("Date"));
                    appointment.put("time", rs.getTime("Time"));
                    appointment.put("duration", rs.getInt("Duration"));
                    appointment.put("visitType", rs.getString("VisitType"));
                    appointment.put("status", rs.getString("Status"));
                    appointment.put("notes", rs.getString("Notes"));
                    appointments.add(appointment);
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving staff appointments: " + e.getMessage());
            }
            
            result.put("appointments", appointments);
        }
        
        return result;
    }
    
    /**
     * Get all doctors (staff with JobType = 'Doctor')
     * @return List of Staff objects who are doctors
     */
    public List<Staff> getDoctors() {
        List<Staff> staffList = new ArrayList<>();
        return staffList;
    }
    
    /**
     * Get all active staff members
     * @return List of active Staff objects
     */
    public List<Staff> getActiveStaff() {
        return getStaffList(null, null, true);
    }
    
    /**
     * Search staff by name
     * @param name Name to search for
     * @return List of Staff objects matching the name
     */
    public List<Staff> searchStaffByName(String name) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Staff WHERE Name LIKE ? ORDER BY Name");
        List<Staff> staffList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                staffList.add(new Staff(
                    rs.getInt("StaffID"),
                    rs.getString("Name"),
                    rs.getString("JobType"),
                    rs.getString("Specialization"),
                    rs.getString("LicenseNumber"),
                    rs.getString("Phone"),
                    rs.getString("Email"),
                    rs.getDate("HireDate"),
                    rs.getString("WorkingDays"),
                    rs.getBoolean("ActiveStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching staff by name: " + e.getMessage());
        }
        
        return staffList;
    }
    
    /**
     * Get staff ID by name
     * @param name Name of staff member
     * @return Staff ID or -1 if not found
     */
    public int getStaffIdByName(String name) {
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