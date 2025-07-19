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
import com.clinicnexus.model.Inventory;

/**
 * Data Access Object for Inventory operations
 */
public class InventoryDAO {
    
    /**
     * Add new inventory item
     * @param inventory Inventory object to add
     * @return true if successful, false otherwise
     */
    public boolean addInventory(Inventory inventory) {
        String sql = "INSERT INTO Inventory (Name, Type, Purpose, StockQuantity, ReorderThreshold, UnitPrice, SupplierInfo, ExpiryDate, ActiveStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, inventory.getName());
            pstmt.setString(2, inventory.getType());
            pstmt.setString(3, inventory.getPurpose());
            pstmt.setInt(4, inventory.getStockQuantity());
            pstmt.setInt(5, inventory.getReorderThreshold());
            pstmt.setDouble(6, inventory.getUnitPrice());
            pstmt.setString(7, inventory.getSupplierInfo());
            pstmt.setDate(8, inventory.getExpiryDate());
            pstmt.setBoolean(9, inventory.isActiveStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding inventory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get inventory item by ID
     * @param itemID ID of inventory item to retrieve
     * @return Inventory object or null if not found
     */
    public Inventory getInventory(int itemID) {
        String sql = "SELECT * FROM Inventory WHERE ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Inventory(
                    rs.getInt("ItemID"),
                    rs.getString("Name"),
                    rs.getString("Type"),
                    rs.getString("Purpose"),
                    rs.getInt("StockQuantity"),
                    rs.getInt("ReorderThreshold"),
                    rs.getDouble("UnitPrice"),
                    rs.getString("SupplierInfo"),
                    rs.getDate("ExpiryDate"),
                    rs.getBoolean("ActiveStatus")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving inventory: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update inventory item
     * @param inventory Inventory object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateInventory(Inventory inventory) {
        String sql = "UPDATE Inventory SET Name = ?, Type = ?, Purpose = ?, StockQuantity = ?, ReorderThreshold = ?, UnitPrice = ?, SupplierInfo = ?, ExpiryDate = ?, ActiveStatus = ? WHERE ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, inventory.getName());
            pstmt.setString(2, inventory.getType());
            pstmt.setString(3, inventory.getPurpose());
            pstmt.setInt(4, inventory.getStockQuantity());
            pstmt.setInt(5, inventory.getReorderThreshold());
            pstmt.setDouble(6, inventory.getUnitPrice());
            pstmt.setString(7, inventory.getSupplierInfo());
            pstmt.setDate(8, inventory.getExpiryDate());
            pstmt.setBoolean(9, inventory.isActiveStatus());
            pstmt.setInt(10, inventory.getItemID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating inventory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete inventory item
     * @param itemID ID of inventory item to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteInventory(int itemID) {
        String sql = "DELETE FROM Inventory WHERE ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting inventory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of inventory items with optional filtering
     * @param nameFilter Filter by name (null for no filter)
     * @param typeFilter Filter by type (null for no filter)
     * @param activeFilter Filter by active status (null for no filter)
     * @return List of Inventory objects
     */
    public List<Inventory> getInventoryList(String nameFilter, String typeFilter, Boolean activeFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Inventory WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            sql.append(" AND Name LIKE ?");
            params.add("%" + nameFilter + "%");
        }
        
        if (typeFilter != null && !typeFilter.trim().isEmpty()) {
            sql.append(" AND Type = ?");
            params.add(typeFilter);
        }
        
        if (activeFilter != null) {
            sql.append(" AND ActiveStatus = ?");
            params.add(activeFilter);
        }
        
        sql.append(" ORDER BY Name");
        
        List<Inventory> inventoryList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                inventoryList.add(new Inventory(
                    rs.getInt("ItemID"),
                    rs.getString("Name"),
                    rs.getString("Type"),
                    rs.getString("Purpose"),
                    rs.getInt("StockQuantity"),
                    rs.getInt("ReorderThreshold"),
                    rs.getDouble("UnitPrice"),
                    rs.getString("SupplierInfo"),
                    rs.getDate("ExpiryDate"),
                    rs.getBoolean("ActiveStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving inventory list: " + e.getMessage());
        }
        
        return inventoryList;
    }
    
    /**
     * Get inventory item with usage history
     * @param itemID ID of inventory item
     * @return Map containing inventory info and usage history
     */
    public Map<String, Object> getInventoryWithUsage(int itemID) {
        Map<String, Object> result = new HashMap<>();
        
        Inventory inventory = getInventory(itemID);
        if (inventory != null) {
            result.put("inventory", inventory);
            
            String sql = "SELECT ai.*, a.Date, a.Time, p.Name as PatientName, s.Name as DoctorName " +
                        "FROM Appointment_Inventory ai " +
                        "JOIN Appointment a ON ai.AppointmentID = a.AppointmentID " +
                        "JOIN Patient p ON a.PatientID = p.PatientID " +
                        "JOIN Staff s ON a.DoctorID = s.StaffID " +
                        "WHERE ai.ItemID = ? ORDER BY a.Date DESC, a.Time DESC";
            
            List<Map<String, Object>> usageHistory = new ArrayList<>();
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, itemID);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> usage = new HashMap<>();
                    usage.put("appointmentID", rs.getInt("AppointmentID"));
                    usage.put("quantityUsed", rs.getInt("QuantityUsed"));
                    usage.put("date", rs.getDate("Date"));
                    usage.put("time", rs.getTime("Time"));
                    usage.put("patientName", rs.getString("PatientName"));
                    usage.put("doctorName", rs.getString("DoctorName"));
                    usageHistory.add(usage);
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving inventory usage: " + e.getMessage());
            }
            
            result.put("usageHistory", usageHistory);
        }
        
        return result;
    }
    
    /**
     * Get items that need reordering
     * @return List of Inventory objects that need reordering
     */
    public List<Inventory> getItemsNeedingReorder() {
        String sql = "SELECT * FROM Inventory WHERE StockQuantity <= ReorderThreshold AND ActiveStatus = true ORDER BY (StockQuantity - ReorderThreshold)";
        
        List<Inventory> needReorderList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                needReorderList.add(new Inventory(
                    rs.getInt("ItemID"),
                    rs.getString("Name"),
                    rs.getString("Type"),
                    rs.getString("Purpose"),
                    rs.getInt("StockQuantity"),
                    rs.getInt("ReorderThreshold"),
                    rs.getDouble("UnitPrice"),
                    rs.getString("SupplierInfo"),
                    rs.getDate("ExpiryDate"),
                    rs.getBoolean("ActiveStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving items needing reorder: " + e.getMessage());
        }
        
        return needReorderList;
    }
    
    /**
     * Get expired items
     * @return List of Inventory objects that have expired
     */
    public List<Inventory> getExpiredItems() {
        String sql = "SELECT * FROM Inventory WHERE ExpiryDate < CURDATE() AND ActiveStatus = true ORDER BY ExpiryDate";
        
        List<Inventory> expiredList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                expiredList.add(new Inventory(
                    rs.getInt("ItemID"),
                    rs.getString("Name"),
                    rs.getString("Type"),
                    rs.getString("Purpose"),
                    rs.getInt("StockQuantity"),
                    rs.getInt("ReorderThreshold"),
                    rs.getDouble("UnitPrice"),
                    rs.getString("SupplierInfo"),
                    rs.getDate("ExpiryDate"),
                    rs.getBoolean("ActiveStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving expired items: " + e.getMessage());
        }
        
        return expiredList;
    }
    
    /**
     * Update stock quantity for an item
     * @param itemID ID of the item
     * @param newQuantity New stock quantity
     * @return true if successful, false otherwise
     */
    public boolean updateStockQuantity(int itemID, int newQuantity) {
        String sql = "UPDATE Inventory SET StockQuantity = ? WHERE ItemID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, itemID);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock quantity: " + e.getMessage());
            return false;
        }
    }
}