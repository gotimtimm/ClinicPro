package com.clinicnexus.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.config.DatabaseConnection;
import com.clinicnexus.dao.InventoryDAO;
import com.clinicnexus.model.Inventory;
import com.clinicnexus.util.InventoryManagementResult;
import com.clinicnexus.util.RestockResult;
import com.clinicnexus.util.UsageResult;

/**
 * Transaction service for inventory management operations
 * Handles Transaction 4.3: Inventory Management
 */
@Service
public class InventoryManagementService {
    
    private InventoryDAO inventoryDAO;
    
    public InventoryManagementService() {
        this.inventoryDAO = new InventoryDAO();
    }
    
    /**
     * Process inventory management operations including low-stock auto-order
     * @return InventoryManagementResult with processing details
     */
    public InventoryManagementResult processInventoryManagement() {
        Connection conn = null;
        List<String> processedItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Check for low-stock items and auto-order
            List<Inventory> lowStockItems = getLowStockItems(conn);
            for (Inventory item : lowStockItems) {
                try {
                    processAutoOrder(conn, item);
                    processedItems.add("Auto-ordered: " + item.getName() + " (Current: " + item.getStockQuantity() + ", Threshold: " + item.getReorderThreshold() + ")");
                } catch (SQLException e) {
                    errors.add("Failed to auto-order " + item.getName() + ": " + e.getMessage());
                }
            }
            
            // Step 2: Process any pending restocking updates
            processPendingRestocking(conn, processedItems, errors);
            
            // Step 3: Update usage tracking from recent appointments
            updateUsageTracking(conn, processedItems, errors);
            
            conn.commit();
            return new InventoryManagementResult(true, processedItems, errors);
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error in inventory management: " + e.getMessage());
            errors.add("System error: " + e.getMessage());
            return new InventoryManagementResult(false, processedItems, errors);
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
     * Manual restocking operation
     * @param itemID ID of item to restock
     * @param quantityReceived Quantity received
     * @param supplierInfo Supplier information
     * @return RestockResult with success status
     */
    public RestockResult processRestocking(int itemID, int quantityReceived, String supplierInfo) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Validate item exists
            Inventory item = getInventoryItem(conn, itemID);
            if (item == null) {
                throw new SQLException("Inventory item not found");
            }
            
            // Step 2: Update stock quantity
            updateStockQuantity(conn, itemID, item.getStockQuantity() + quantityReceived);
            
            // Step 3: Update supplier info if provided
            if (supplierInfo != null && !supplierInfo.trim().isEmpty()) {
                updateSupplierInfo(conn, itemID, supplierInfo);
            }
            
            // Step 4: Log restocking activity
            logRestockingActivity(conn, itemID, quantityReceived, supplierInfo);
            
            conn.commit();
            return new RestockResult(true, "Successfully restocked " + item.getName() + " with " + quantityReceived + " units");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error processing restocking: " + e.getMessage());
            return new RestockResult(false, e.getMessage());
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
     * Process inventory usage from appointments
     * @param appointmentID Appointment ID
     * @param inventoryUsage Map of item ID to quantity used
     * @return UsageResult with processing status
     */
    public UsageResult processInventoryUsage(int appointmentID, Map<Integer, Integer> inventoryUsage) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Validate appointment exists
            if (!appointmentExists(conn, appointmentID)) {
                throw new SQLException("Appointment not found");
            }
            
            // Step 2: Validate all items have sufficient stock
            for (Map.Entry<Integer, Integer> entry : inventoryUsage.entrySet()) {
                int itemID = entry.getKey();
                int quantityUsed = entry.getValue();
                
                if (!hasSufficientStock(conn, itemID, quantityUsed)) {
                    Inventory item = getInventoryItem(conn, itemID);
                    String itemName = item != null ? item.getName() : "Item ID " + itemID;
                    throw new SQLException("Insufficient stock for " + itemName);
                }
            }
            
            // Step 3: Record usage and update stock
            List<String> processedItems = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : inventoryUsage.entrySet()) {
                int itemID = entry.getKey();
                int quantityUsed = entry.getValue();
                
                // Record usage
                recordInventoryUsage(conn, appointmentID, itemID, quantityUsed);
                
                // Update stock
                Inventory item = getInventoryItem(conn, itemID);
                updateStockQuantity(conn, itemID, item.getStockQuantity() - quantityUsed);
                
                processedItems.add(item.getName() + " (Used: " + quantityUsed + ")");
            }
            
            // Step 4: Check for items that now need reordering
            List<String> reorderAlerts = checkForReorderAlerts(conn, new ArrayList<>(inventoryUsage.keySet()));
            
            conn.commit();
            return new UsageResult(true, "Inventory usage processed successfully", processedItems, reorderAlerts);
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Error processing inventory usage: " + e.getMessage());
            return new UsageResult(false, e.getMessage(), new ArrayList<>(), new ArrayList<>());
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
     * Get low stock items
     */
    private List<Inventory> getLowStockItems(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Inventory WHERE StockQuantity <= ReorderThreshold AND ActiveStatus = true";
        List<Inventory> lowStockItems = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                lowStockItems.add(new Inventory(
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
        }
        
        return lowStockItems;
    }
    
    /**
     * Process auto-order for low stock items
     */
    private void processAutoOrder(Connection conn, Inventory item) throws SQLException {
        // Calculate reorder quantity (2x threshold or minimum 50 units)
        int reorderQuantity = Math.max(item.getReorderThreshold() * 2, 50);
        
        // In a real system, this would integrate with supplier APIs
        // For now, we'll simulate by creating a purchase order record
        createPurchaseOrder(conn, item.getItemID(), reorderQuantity);
        
        // Log the auto-order
        logAutoOrder(conn, item.getItemID(), reorderQuantity);
        
        // Notify supplier (placeholder)
        notifySupplier(item, reorderQuantity);
    }
    
    /**
     * Create purchase order record
     */
    private void createPurchaseOrder(Connection conn, int itemID, int quantity) throws SQLException {
        // This would typically be in a separate purchase_orders table
        // For this example, we'll just log it
        System.out.println("Purchase order created for Item ID: " + itemID + ", Quantity: " + quantity);
    }
    
    /**
     * Log auto-order activity
     */
    private void logAutoOrder(Connection conn, int itemID, int quantity) throws SQLException {
        // This would typically be in a separate inventory_logs table
        // For this example, we'll just log it
        System.out.println("Auto-order logged: Item ID " + itemID + ", Quantity: " + quantity + ", Date: " + new java.sql.Date(System.currentTimeMillis()));
    }
    
    /**
     * Notify supplier (placeholder)
     */
    private void notifySupplier(Inventory item, int quantity) {
        System.out.println("Supplier notification sent for " + item.getName() + " - Quantity: " + quantity);
        System.out.println("Supplier: " + item.getSupplierInfo());
    }
    
    /**
     * Process pending restocking
     */
    private void processPendingRestocking(Connection conn, List<String> processedItems, List<String> errors) throws SQLException {
        // This would check for pending deliveries/restocking
        // For this example, we'll just log that the check was performed
        processedItems.add("Checked for pending restocking operations");
    }
    
    /**
     * Update usage tracking
     */
    private void updateUsageTracking(Connection conn, List<String> processedItems, List<String> errors) throws SQLException {
        // This would update usage statistics and trends
        // For this example, we'll just log that tracking was updated
        processedItems.add("Usage tracking updated");
    }
    
    /**
     * Get inventory item
     */
    private Inventory getInventoryItem(Connection conn, int itemID) throws SQLException {
        String sql = "SELECT * FROM Inventory WHERE ItemID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        }
        return null;
    }
    
    /**
     * Update stock quantity
     */
    private void updateStockQuantity(Connection conn, int itemID, int newQuantity) throws SQLException {
        String sql = "UPDATE Inventory SET StockQuantity = ? WHERE ItemID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, itemID);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update supplier info
     */
    private void updateSupplierInfo(Connection conn, int itemID, String supplierInfo) throws SQLException {
        String sql = "UPDATE Inventory SET SupplierInfo = ? WHERE ItemID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplierInfo);
            pstmt.setInt(2, itemID);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Log restocking activity
     */
    private void logRestockingActivity(Connection conn, int itemID, int quantityReceived, String supplierInfo) throws SQLException {
        // This would typically be in a separate restocking_logs table
        System.out.println("Restocking logged: Item ID " + itemID + ", Quantity: " + quantityReceived + ", Supplier: " + supplierInfo);
    }
    
    /**
     * Check if appointment exists
     */
    private boolean appointmentExists(Connection conn, int appointmentID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Appointment WHERE AppointmentID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    /**
     * Check if item has sufficient stock
     */
    private boolean hasSufficientStock(Connection conn, int itemID, int quantityNeeded) throws SQLException {
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
     * Check for reorder alerts
     */
    private List<String> checkForReorderAlerts(Connection conn, List<Integer> itemIDs) throws SQLException {
        List<String> alerts = new ArrayList<>();
        
        String sql = "SELECT Name, StockQuantity, ReorderThreshold FROM Inventory WHERE ItemID = ? AND StockQuantity <= ReorderThreshold";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Integer itemID : itemIDs) {
                pstmt.setInt(1, itemID);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String name = rs.getString("Name");
                    int stock = rs.getInt("StockQuantity");
                    int threshold = rs.getInt("ReorderThreshold");
                    alerts.add("REORDER ALERT: " + name + " (Stock: " + stock + ", Threshold: " + threshold + ")");
                }
            }
        }
        
        return alerts;
    }
}
