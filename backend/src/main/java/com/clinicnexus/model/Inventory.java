package com.clinicnexus.model;

import java.sql.Date;

/**
 * Inventory model class representing medical supplies and equipment
 */
public class Inventory {
    private int itemID;
    private String name;
    private String type;
    private String purpose;
    private int stockQuantity;
    private int reorderThreshold;
    private double unitPrice;
    private String supplierInfo;
    private Date expiryDate;
    private boolean activeStatus;
    
    /**
     * Default constructor
     */
    public Inventory() {}
    
    /**
     * Constructor with all fields
     */
    public Inventory(int itemID, String name, String type, String purpose, 
                    int stockQuantity, int reorderThreshold, double unitPrice, 
                    String supplierInfo, Date expiryDate, boolean activeStatus) {
        this.itemID = itemID;
        this.name = name;
        this.type = type;
        this.purpose = purpose;
        this.stockQuantity = stockQuantity;
        this.reorderThreshold = reorderThreshold;
        this.unitPrice = unitPrice;
        this.supplierInfo = supplierInfo;
        this.expiryDate = expiryDate;
        this.activeStatus = activeStatus;
    }
    
    // Getters and Setters
    public int getItemID() { return itemID; }
    public void setItemID(int itemID) { this.itemID = itemID; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public int getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(int reorderThreshold) { this.reorderThreshold = reorderThreshold; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public String getSupplierInfo() { return supplierInfo; }
    public void setSupplierInfo(String supplierInfo) { this.supplierInfo = supplierInfo; }
    
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    
    public boolean isActiveStatus() { return activeStatus; }
    public void setActiveStatus(boolean activeStatus) { this.activeStatus = activeStatus; }
    
    /**
     * Check if item needs reordering
     * @return true if stock is at or below reorder threshold
     */
    public boolean needsReordering() {
        return stockQuantity <= reorderThreshold;
    }
    
    /**
     * Check if item is expired
     * @return true if item has expired
     */
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.before(new Date(System.currentTimeMillis()));
    }
    
    @Override
    public String toString() {
        return "Inventory{" +
                "itemID=" + itemID +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", purpose='" + purpose + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", reorderThreshold=" + reorderThreshold +
                ", unitPrice=" + unitPrice +
                ", supplierInfo='" + supplierInfo + '\'' +
                ", expiryDate=" + expiryDate +
                ", activeStatus=" + activeStatus +
                '}';
    }
}