package com.clinicnexus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.model.Billing;

/**
 * Data Access Object for Billing operations
 */
public class BillingDAO {
    
    /**
     * Add new billing record
     * @param billing Billing object to add
     * @return billing ID if successful, -1 otherwise
     */
    public int addBilling(Billing billing) {
        String sql = "INSERT INTO Billing (AppointmentID, Amount, Paid, PaymentDate) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, billing.getAppointmentID());
            pstmt.setDouble(2, billing.getAmount());
            pstmt.setBoolean(3, billing.isPaid());
            pstmt.setDate(4, billing.getPaymentDate());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding billing: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Get billing by ID
     * @param billingID ID of billing to retrieve
     * @return Billing object or null if not found
     */
    public Billing getBilling(int billingID) {
        String sql = "SELECT * FROM Billing WHERE BillingID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billingID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Billing(
                    rs.getInt("BillingID"),
                    rs.getInt("AppointmentID"),
                    rs.getDouble("Amount"),
                    rs.getBoolean("Paid"),
                    rs.getDate("PaymentDate")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving billing: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update billing information
     * @param billing Billing object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateBilling(Billing billing) {
        String sql = "UPDATE Billing SET AppointmentID = ?, Amount = ?, Paid = ?, PaymentDate = ? WHERE BillingID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billing.getAppointmentID());
            pstmt.setDouble(2, billing.getAmount());
            pstmt.setBoolean(3, billing.isPaid());
            pstmt.setDate(4, billing.getPaymentDate());
            pstmt.setInt(5, billing.getBillingID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating billing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete billing record
     * @param billingID ID of billing to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteBilling(int billingID) {
        String sql = "DELETE FROM Billing WHERE BillingID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billingID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting billing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of billing records with optional filtering
     * @param appointmentID Filter by appointment ID (null for no filter)
     * @param paidFilter Filter by paid status (null for no filter)
     * @return List of Billing objects
     */
    public List<Billing> getBillingList(Integer appointmentID, Boolean paidFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Billing WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (appointmentID != null) {
            sql.append(" AND AppointmentID = ?");
            params.add(appointmentID);
        }
        
        if (paidFilter != null) {
            sql.append(" AND Paid = ?");
            params.add(paidFilter);
        }
        
        sql.append(" ORDER BY BillingID DESC");
        
        List<Billing> billingList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                billingList.add(new Billing(
                    rs.getInt("BillingID"),
                    rs.getInt("AppointmentID"),
                    rs.getDouble("Amount"),
                    rs.getBoolean("Paid"),
                    rs.getDate("PaymentDate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving billing list: " + e.getMessage());
        }
        
        return billingList;
    }
    
    /**
     * Get billing by appointment ID
     * @param appointmentID ID of appointment
     * @return Billing object or null if not found
     */
    public Billing getBillingByAppointment(int appointmentID) {
        String sql = "SELECT * FROM Billing WHERE AppointmentID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Billing(
                    rs.getInt("BillingID"),
                    rs.getInt("AppointmentID"),
                    rs.getDouble("Amount"),
                    rs.getBoolean("Paid"),
                    rs.getDate("PaymentDate")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving billing by appointment: " + e.getMessage());
        }
        return null;
    }
} 