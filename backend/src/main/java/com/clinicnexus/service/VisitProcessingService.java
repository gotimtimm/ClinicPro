package com.clinicnexus.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.dao.AppointmentDAO;
import com.clinicnexus.dao.InventoryDAO;
import com.clinicnexus.dao.PatientDAO;
import com.clinicnexus.model.Appointment;
import com.clinicnexus.util.AppointmentResult;
import com.clinicnexus.util.VisitProcessingData;
import com.clinicnexus.util.VisitResult;

/**
 * Transaction service for patient visit processing operations
 * Handles Transaction 4.2: Patient Visit Processing
 */
@Service
public class VisitProcessingService {
    
    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final InventoryDAO inventoryDAO;
    private final AppointmentTransactionService appointmentService;
    
    public VisitProcessingService() {
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.inventoryDAO = new InventoryDAO();
        this.appointmentService = new AppointmentTransactionService();
    }
    
    /**
     * Process a patient visit with all related operations
     * @param visitData Visit processing data
     * @return VisitResult with success status and details
     */
    public VisitResult processPatientVisit(VisitProcessingData visitData) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Update appointment status to 'Done'
            updateAppointmentStatus(conn, visitData.getAppointmentID(), "Done");
            
            // Step 2: Record health metrics (vital signs)
            recordHealthMetrics(conn, visitData);
            
            // Step 3: Log diagnosis and treatment
            logDiagnosisAndTreatment(conn, visitData);
            
            // Step 4: Process medication/inventory usage
            if (visitData.getInventoryUsage() != null && !visitData.getInventoryUsage().isEmpty()) {
                processInventoryUsage(conn, visitData.getAppointmentID(), visitData.getInventoryUsage());
            }
            
            // Step 5: Generate/update bill
            generateBill(conn, visitData);
            
            // Step 6: Schedule follow-up appointment if needed
            if (visitData.isScheduleFollowUp()) {
                scheduleFollowUpAppointment(conn, visitData);
            }
            
            conn.commit();
            return new VisitResult(true, "Patient visit processed successfully");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error processing patient visit: " + e.getMessage());
            return new VisitResult(false, e.getMessage());
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
     * Update appointment status
     */
    private void updateAppointmentStatus(Connection conn, int appointmentID, String newStatus) throws SQLException {
        String sql = "UPDATE Appointment SET Status = ? WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentID);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Appointment not found or could not be updated");
            }
        }
    }
    
    /**
     * Record health metrics (vital signs)
     */
    private void recordHealthMetrics(Connection conn, VisitProcessingData visitData) throws SQLException {
        // In a real implementation, this would update a separate health_metrics table
        // For now, we'll update the appointment notes with vital signs
        String currentNotes = getCurrentAppointmentNotes(conn, visitData.getAppointmentID());
        String vitalSigns = formatVitalSigns(visitData.getVitalSigns());
        String updatedNotes = currentNotes + "\n\nVital Signs: " + vitalSigns;
        
        String sql = "UPDATE Appointment SET Notes = ? WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, updatedNotes);
            pstmt.setInt(2, visitData.getAppointmentID());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get current appointment notes
     */
    private String getCurrentAppointmentNotes(Connection conn, int appointmentID) throws SQLException {
        String sql = "SELECT Notes FROM Appointment WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String notes = rs.getString("Notes");
                return notes != null ? notes : "";
            }
        }
        return "";
    }
    
    /**
     * Format vital signs for storage
     */
    private String formatVitalSigns(Map<String, String> vitalSigns) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : vitalSigns.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // Remove last comma and space
        }
        return sb.toString();
    }
    
    /**
     * Log diagnosis and treatment
     */
    private void logDiagnosisAndTreatment(Connection conn, VisitProcessingData visitData) throws SQLException {
        // Update appointment notes with diagnosis and treatment
        String currentNotes = getCurrentAppointmentNotes(conn, visitData.getAppointmentID());
        String diagnosisInfo = "\n\nDiagnosis: " + visitData.getDiagnosis();
        String treatmentInfo = "\nTreatment: " + visitData.getTreatment();
        String updatedNotes = currentNotes + diagnosisInfo + treatmentInfo;
        
        String sql = "UPDATE Appointment SET Notes = ? WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, updatedNotes);
            pstmt.setInt(2, visitData.getAppointmentID());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Process inventory usage during the visit
     */
    private void processInventoryUsage(Connection conn, int appointmentID, Map<Integer, Integer> inventoryUsage) throws SQLException {
        // Check inventory availability
        for (Map.Entry<Integer, Integer> entry : inventoryUsage.entrySet()) {
            int itemID = entry.getKey();
            int quantityUsed = entry.getValue();
            
            if (!checkInventoryAvailability(conn, itemID, quantityUsed)) {
                throw new SQLException("Insufficient inventory for item ID: " + itemID);
            }
        }
        
        // Record usage and update inventory
        for (Map.Entry<Integer, Integer> entry : inventoryUsage.entrySet()) {
            int itemID = entry.getKey();
            int quantityUsed = entry.getValue();
            
            // Record usage in appointment_inventory table
            recordInventoryUsage(conn, appointmentID, itemID, quantityUsed);
            
            // Update inventory stock
            updateInventoryStock(conn, itemID, quantityUsed);
        }
    }
    
    /**
     * Check if inventory is available
     */
    private boolean checkInventoryAvailability(Connection conn, int itemID, int quantityNeeded) throws SQLException {
        String sql = "SELECT StockQuantity FROM Inventory WHERE ItemID = ? AND ActiveStatus = true";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int currentStock = rs.getInt("StockQuantity");
                return currentStock >= quantityNeeded;
            }
        }
        return false;
    }
    
    /**
     * Record inventory usage
     */
    private void recordInventoryUsage(Connection conn, int appointmentID, int itemID, int quantityUsed) throws SQLException {
        String sql = "INSERT INTO Appointment_Inventory (AppointmentID, ItemID, QuantityUsed) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE QuantityUsed = QuantityUsed + ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentID);
            pstmt.setInt(2, itemID);
            pstmt.setInt(3, quantityUsed);
            pstmt.setInt(4, quantityUsed);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update inventory stock
     */
    private void updateInventoryStock(Connection conn, int itemID, int quantityUsed) throws SQLException {
        String sql = "UPDATE Inventory SET StockQuantity = StockQuantity - ? WHERE ItemID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityUsed);
            pstmt.setInt(2, itemID);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Generate or update bill for the visit
     */
    private void generateBill(Connection conn, VisitProcessingData visitData) throws SQLException {
        double totalAmount = calculateTotalAmount(conn, visitData);
        
        // Update existing billing record
        String sql = "UPDATE Billing SET Amount = ? WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, totalAmount);
            pstmt.setInt(2, visitData.getAppointmentID());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                // Create new billing record if it doesn't exist
                createBillingRecord(conn, visitData.getAppointmentID(), totalAmount);
            }
        }
    }
    
    /**
     * Calculate total amount for the visit
     */
    private double calculateTotalAmount(Connection conn, VisitProcessingData visitData) throws SQLException {
        double baseAmount = visitData.getBaseAmount();
        double inventoryAmount = 0.0;
        
        // Calculate inventory costs
        if (visitData.getInventoryUsage() != null) {
            for (Map.Entry<Integer, Integer> entry : visitData.getInventoryUsage().entrySet()) {
                int itemID = entry.getKey();
                int quantityUsed = entry.getValue();
                
                String sql = "SELECT UnitPrice FROM Inventory WHERE ItemID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, itemID);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        double unitPrice = rs.getDouble("UnitPrice");
                        inventoryAmount += unitPrice * quantityUsed;
                    }
                }
            }
        }
        
        return baseAmount + inventoryAmount;
    }
    
    /**
     * Create billing record
     */
    private void createBillingRecord(Connection conn, int appointmentID, double amount) throws SQLException {
        String sql = "INSERT INTO Billing (AppointmentID, Amount, Paid) VALUES (?, ?, false)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentID);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Schedule follow-up appointment
     */
    private void scheduleFollowUpAppointment(Connection conn, VisitProcessingData visitData) throws SQLException {
        // Get original appointment details
        Appointment originalAppointment = getAppointmentDetails(conn, visitData.getAppointmentID());
        
        if (originalAppointment != null && visitData.getFollowUpDate() != null) {
            Appointment followUpAppointment = new Appointment();
            followUpAppointment.setPatientID(originalAppointment.getPatientID());
            followUpAppointment.setDoctorID(originalAppointment.getDoctorID());
            followUpAppointment.setDate(visitData.getFollowUpDate());
            followUpAppointment.setTime(visitData.getFollowUpTime());
            followUpAppointment.setDuration(30); // Default duration
            followUpAppointment.setVisitType("Check-up"); // Default follow-up type
            followUpAppointment.setStatus("Not Done");
            followUpAppointment.setNotes("Follow-up for appointment #" + visitData.getAppointmentID());
            
            // Use appointment service to schedule follow-up
            AppointmentResult result = appointmentService.scheduleAppointment(followUpAppointment);
            if (!result.isSuccess()) {
                throw new SQLException("Failed to schedule follow-up appointment: " + result.getMessage());
            }
        }
    }
    
    /**
     * Get appointment details
     */
    private Appointment getAppointmentDetails(Connection conn, int appointmentID) throws SQLException {
        String sql = "SELECT * FROM Appointment WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        }
        return null;
    }
}
