package com.clinicnexus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.model.AppointmentInventory;

/**
 * Data Access Object for Appointment_Inventory operations
 */
public class AppointmentInventoryDAO {
    
    /**
     * Add new appointment inventory record
     * @param appointmentInventory AppointmentInventory object to add
     * @return true if successful, false otherwise
     */
    public boolean addAppointmentInventory(AppointmentInventory appointmentInventory) {
        String sql = "INSERT INTO Appointment_Inventory (AppointmentID, ItemID, QuantityUsed) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentInventory.getAppointmentID());
            pstmt.setInt(2, appointmentInventory.getItemID());
            pstmt.setInt(3, appointmentInventory.getQuantityUsed());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding appointment inventory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get appointment inventory by composite key
     * @param appointmentID ID of appointment
     * @param itemID ID of inventory item
     * @return AppointmentInventory object or null if not found
     */
    public AppointmentInventory getAppointmentInventory(int appointmentID, int itemID) {
        String sql = "SELECT * FROM Appointment_Inventory WHERE AppointmentID = ? AND ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentID);
            pstmt.setInt(2, itemID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new AppointmentInventory(
                    rs.getInt("AppointmentID"),
                    rs.getInt("ItemID"),
                    rs.getInt("QuantityUsed")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment inventory: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update appointment inventory information
     * @param appointmentInventory AppointmentInventory object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateAppointmentInventory(AppointmentInventory appointmentInventory) {
        String sql = "UPDATE Appointment_Inventory SET QuantityUsed = ? WHERE AppointmentID = ? AND ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentInventory.getQuantityUsed());
            pstmt.setInt(2, appointmentInventory.getAppointmentID());
            pstmt.setInt(3, appointmentInventory.getItemID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment inventory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete appointment inventory record by composite key
     * @param appointmentID ID of appointment
     * @param itemID ID of inventory item
     * @return true if successful, false otherwise
     */
    public boolean deleteAppointmentInventory(int appointmentID, int itemID) {
        String sql = "DELETE FROM Appointment_Inventory WHERE AppointmentID = ? AND ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentID);
            pstmt.setInt(2, itemID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting appointment inventory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of appointment inventory records with optional filtering
     * @param appointmentID Filter by appointment ID (null for no filter)
     * @param itemID Filter by item ID (null for no filter)
     * @return List of AppointmentInventory objects
     */
    public List<AppointmentInventory> getAppointmentInventoryList(Integer appointmentID, Integer itemID) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Appointment_Inventory WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (appointmentID != null) {
            sql.append(" AND AppointmentID = ?");
            params.add(appointmentID);
        }
        
        if (itemID != null) {
            sql.append(" AND ItemID = ?");
            params.add(itemID);
        }
        
        sql.append(" ORDER BY AppointmentID DESC, ItemID");
        
        List<AppointmentInventory> appointmentInventoryList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                appointmentInventoryList.add(new AppointmentInventory(
                    rs.getInt("AppointmentID"),
                    rs.getInt("ItemID"),
                    rs.getInt("QuantityUsed")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment inventory list: " + e.getMessage());
        }
        
        return appointmentInventoryList;
    }
    
    /**
     * Get all inventory usage for a specific appointment
     * @param appointmentID ID of appointment
     * @return List of AppointmentInventory objects
     */
    public List<AppointmentInventory> getInventoryUsageByAppointment(int appointmentID) {
        String sql = "SELECT ai.*, i.Name as ItemName, i.Type as ItemType " +
                    "FROM Appointment_Inventory ai " +
                    "JOIN Inventory i ON ai.ItemID = i.ItemID " +
                    "WHERE ai.AppointmentID = ? " +
                    "ORDER BY i.Type, i.Name";
        
        List<AppointmentInventory> usageList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AppointmentInventory usage = new AppointmentInventory(
                    rs.getInt("AppointmentID"),
                    rs.getInt("ItemID"),
                    rs.getInt("QuantityUsed")
                );
                // Note: If you want to include item details, you'd need to extend the model
                usageList.add(usage);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving inventory usage by appointment: " + e.getMessage());
        }
        
        return usageList;
    }
} 