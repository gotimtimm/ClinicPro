package com.clinicnexus.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.dao.AppointmentDAO;
import com.clinicnexus.dao.StaffDAO;
import com.clinicnexus.model.Appointment;
import com.clinicnexus.util.CoverageResult;
import com.clinicnexus.util.SchedulingResult;
import com.clinicnexus.util.TimeOffResult;

/**
 * Transaction service for staff scheduling operations
 * Handles Transaction 4.4: Staff Scheduling
 */
@Service
public class StaffSchedulingService {
    
    private final StaffDAO staffDAO;
    private final AppointmentDAO appointmentDAO;
    
    public StaffSchedulingService() {
        this.staffDAO = new StaffDAO();
        this.appointmentDAO = new AppointmentDAO();
    }
    
    /**
     * Schedule staff shift with validation
     * @param staffID Staff member ID
     * @param shiftDate Date of the shift
     * @param startTime Start time of shift
     * @param endTime End time of shift
     * @return SchedulingResult with success status
     */
    public SchedulingResult scheduleStaffShift(int staffID, Date shiftDate, Time startTime, Time endTime) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Prevent double-booking - validate against existing schedules
            if (hasScheduleConflict(conn, staffID, shiftDate, startTime, endTime)) {
                throw new SQLException("Staff member already has a conflicting schedule");
            }
            
            // Step 2: Validate staff member exists and is active
            if (!isStaffValid(conn, staffID)) {
                throw new SQLException("Staff member not found or inactive");
            }
            
            // Step 3: Create shift schedule record
            createShiftSchedule(conn, staffID, shiftDate, startTime, endTime);
            
            // Step 4: Validate minimum coverage requirements
            if (!hasMinimumCoverage(conn, shiftDate, startTime, endTime)) {
                System.out.println("Warning: Minimum coverage requirements may not be met for " + shiftDate);
            }
            
            conn.commit();
            return new SchedulingResult(true, "Staff shift scheduled successfully");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error scheduling staff shift: " + e.getMessage());
            return new SchedulingResult(false, e.getMessage());
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
     * Process time-off request
     * @param staffID Staff member ID
     * @param startDate Start date of time off
     * @param endDate End date of time off
     * @param reason Reason for time off
     * @return TimeOffResult with approval status
     */
    public TimeOffResult processTimeOffRequest(int staffID, Date startDate, Date endDate, String reason) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Validate staff member
            if (!isStaffValid(conn, staffID)) {
                throw new SQLException("Staff member not found or inactive");
            }
            
            // Step 2: Check for existing appointments during time-off period
            List<Appointment> conflictingAppointments = getConflictingAppointments(conn, staffID, startDate, endDate);
            if (!conflictingAppointments.isEmpty()) {
                throw new SQLException("Cannot approve time-off: " + conflictingAppointments.size() + " existing appointments during this period");
            }
            
            // Step 3: Check minimum coverage requirements
            if (!canApproveTimeOff(conn, staffID, startDate, endDate)) {
                throw new SQLException("Cannot approve time-off: Would violate minimum coverage requirements");
            }
            
            // Step 4: Create time-off record
            createTimeOffRecord(conn, staffID, startDate, endDate, reason, "Approved");
            
            // Step 5: Update staff working days/schedule
            updateStaffScheduleForTimeOff(conn, staffID, startDate, endDate);
            
            conn.commit();
            return new TimeOffResult(true, "Time-off request approved", "Approved");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error processing time-off request: " + e.getMessage());
            return new TimeOffResult(false, e.getMessage(), "Rejected");
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
     * Check staff allocation and ensure minimum coverage
     * @param date Date to check
     * @param shift Shift type (e.g., "Morning", "Evening")
     * @return CoverageResult with coverage status
     */
    public CoverageResult checkStaffCoverage(Date date, String shift) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Get current staff scheduled for the date
            List<Map<String, Object>> scheduledStaff = getScheduledStaff(conn, date, shift);
            
            // Count by job type
            int doctorCount = 0;
            int nurseCount = 0;
            int adminCount = 0;
            
            for (Map<String, Object> staff : scheduledStaff) {
                String jobType = (String) staff.get("jobType");
                switch (jobType) {
                    case "Doctor": doctorCount++; break;
                    case "Nurse": nurseCount++; break;
                    case "Admin": adminCount++; break;
                }
            }
            
            // Check minimum requirements (can be configurable)
            boolean hasMinimumCoverage = doctorCount >= 2 && nurseCount >= 1 && adminCount >= 1;
            
            String message = String.format("Coverage for %s: Doctors: %d, Nurses: %d, Admin: %d", 
                                          date, doctorCount, nurseCount, adminCount);
            
            return new CoverageResult(hasMinimumCoverage, message, doctorCount, nurseCount, adminCount);
            
        } catch (SQLException e) {
            System.err.println("Error checking staff coverage: " + e.getMessage());
            return new CoverageResult(false, "Error checking coverage: " + e.getMessage(), 0, 0, 0);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check for schedule conflicts
     */
    private boolean hasScheduleConflict(Connection conn, int staffID, Date shiftDate, Time startTime, Time endTime) throws SQLException {
        // This would check against a staff_schedules table
        // For this example, we'll check against existing appointments
        String sql = "SELECT COUNT(*) FROM Appointment WHERE DoctorID = ? AND Date = ? AND Status != 'Canceled'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffID);
            pstmt.setDate(2, shiftDate);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int appointmentCount = rs.getInt(1);
                // If there are appointments, check if they conflict with the shift
                return appointmentCount > 0 && !isTimeWithinShift(startTime, endTime);
            }
        }
        return false;
    }
    
    /**
     * Check if time is within shift hours
     */
    private boolean isTimeWithinShift(Time startTime, Time endTime) {
        // Simple check - in real implementation, this would be more sophisticated
        return startTime.before(endTime);
    }
    
    /**
     * Validate staff member
     */
    private boolean isStaffValid(Connection conn, int staffID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Staff WHERE StaffID = ? AND ActiveStatus = true";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    /**
     * Create shift schedule record
     */
    private void createShiftSchedule(Connection conn, int staffID, Date shiftDate, Time startTime, Time endTime) throws SQLException {
        // This would insert into a staff_schedules table
        // For this example, we'll just log it
        System.out.println("Shift scheduled for Staff ID: " + staffID + " on " + shiftDate + " from " + startTime + " to " + endTime);
    }
    
    /**
     * Check minimum coverage requirements
     */
    private boolean hasMinimumCoverage(Connection conn, Date shiftDate, Time startTime, Time endTime) throws SQLException {
        // This would check against minimum staffing requirements
        // For this example, we'll return true
        return true;
    }
    
    /**
     * Get conflicting appointments
     */
    private List<Appointment> getConflictingAppointments(Connection conn, int staffID, Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT * FROM Appointment WHERE DoctorID = ? AND Date BETWEEN ? AND ? AND Status != 'Canceled'";
        List<Appointment> conflicts = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffID);
            pstmt.setDate(2, startDate);
            pstmt.setDate(3, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                conflicts.add(new Appointment(
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
        }
        
        return conflicts;
    }
    
    /**
     * Check if time-off can be approved
     */
    private boolean canApproveTimeOff(Connection conn, int staffID, Date startDate, Date endDate) throws SQLException {
        // This would check if approving time-off would violate minimum coverage
        // For this example, we'll return true
        return true;
    }
    
    /**
     * Create time-off record
     */
    private void createTimeOffRecord(Connection conn, int staffID, Date startDate, Date endDate, String reason, String status) throws SQLException {
        // This would insert into a time_off_requests table
        // For this example, we'll just log it
        System.out.println("Time-off record created for Staff ID: " + staffID + " from " + startDate + " to " + endDate + " - " + status);
    }
    
    /**
     * Update staff schedule for time-off
     */
    private void updateStaffScheduleForTimeOff(Connection conn, int staffID, Date startDate, Date endDate) throws SQLException {
        // This would update the staff schedule to reflect approved time-off
        // For this example, we'll just log it
        System.out.println("Staff schedule updated for time-off: Staff ID " + staffID + " from " + startDate + " to " + endDate);
    }
    
    /**
     * Get scheduled staff for a date and shift
     */
    private List<Map<String, Object>> getScheduledStaff(Connection conn, Date date, String shift) throws SQLException {
        List<Map<String, Object>> scheduledStaff = new ArrayList<>();
        
        // This would query a staff_schedules table
        // For this example, we'll get all active staff
        String sql = "SELECT StaffID, Name, JobType FROM Staff WHERE ActiveStatus = true";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> staff = new HashMap<>();
                staff.put("staffID", rs.getInt("StaffID"));
                staff.put("name", rs.getString("Name"));
                staff.put("jobType", rs.getString("JobType"));
                scheduledStaff.add(staff);
            }
        }
        
        return scheduledStaff;
    }
    
    /**
     * Get staff schedule for a date range
     */
    public List<Map<String, Object>> getStaffSchedule(Date startDate, Date endDate) {
        List<Map<String, Object>> schedule = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.StaffID, s.Name, s.JobType, a.Date, a.Time " +
                        "FROM Staff s " +
                        "LEFT JOIN Appointment a ON s.StaffID = a.DoctorID " +
                        "WHERE s.ActiveStatus = true " +
                        "AND (a.Date BETWEEN ? AND ? OR a.Date IS NULL) " +
                        "ORDER BY s.Name, a.Date, a.Time";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, startDate);
                pstmt.setDate(2, endDate);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> scheduleItem = new HashMap<>();
                    scheduleItem.put("staffID", rs.getInt("StaffID"));
                    scheduleItem.put("name", rs.getString("Name"));
                    scheduleItem.put("jobType", rs.getString("JobType"));
                    scheduleItem.put("date", rs.getDate("Date"));
                    scheduleItem.put("time", rs.getTime("Time"));
                    schedule.add(scheduleItem);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting staff schedule: " + e.getMessage());
        }
        
        return schedule;
    }
}
