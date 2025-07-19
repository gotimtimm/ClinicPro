package com.clinicnexus.util;

import java.util.List;

public class UsageResult {
    private final boolean success;
    private final String message;
    private final List<String> processedItems;
    private final List<String> reorderAlerts;
    
    public UsageResult(boolean success, String message, List<String> processedItems, List<String> reorderAlerts) {
        this.success = success;
        this.message = message;
        this.processedItems = processedItems;
        this.reorderAlerts = reorderAlerts;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<String> getProcessedItems() { return processedItems; }
    public List<String> getReorderAlerts() { return reorderAlerts; }
}