package com.clinicnexus.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.dao.InventoryDAO;
import com.clinicnexus.model.Inventory;

@Service
public class InventoryService {
    private final InventoryDAO inventoryDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
    }

    public boolean createInventory(Inventory inventory) {
        return inventoryDAO.addInventory(inventory);
    }

    public Inventory getInventory(int itemID) {
        return inventoryDAO.getInventory(itemID);
    }

    public boolean updateInventory(Inventory inventory) {
        return inventoryDAO.updateInventory(inventory);
    }

    public boolean deleteInventory(int itemID) {
        return inventoryDAO.deleteInventory(itemID);
    }

    public List<Inventory> listInventory(String nameFilter, String typeFilter, Boolean activeFilter) {
        return inventoryDAO.getInventoryList(nameFilter, typeFilter, activeFilter);
    }

    public Map<String, Object> getInventoryWithUsage(int itemID) {
        return inventoryDAO.getInventoryWithUsage(itemID);
    }

    public List<Inventory> getItemsNeedingReorder() {
        return inventoryDAO.getItemsNeedingReorder();
    }

    public List<Inventory> getExpiredItems() {
        return inventoryDAO.getExpiredItems();
    }
}