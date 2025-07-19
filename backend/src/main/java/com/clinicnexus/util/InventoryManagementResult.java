package com.clinicnexus.util;

import java.util.List;

/**
 * Result class for inventory operations
 */
public class InventoryManagementResult {
    private final boolean success;
    private final List<String> processedItems;
    private final List<String> errors;
    
    public InventoryManagementResult(boolean success, List<String> processedItems, List<String> errors) {
        this.success = success;
        this.processedItems = processedItems;
        this.errors = errors;
    }
    
    public boolean isSuccess() { return success; }
    public List<String> getProcessedItems() { return processedItems; }
    public List<String> getErrors() { return errors; }
}