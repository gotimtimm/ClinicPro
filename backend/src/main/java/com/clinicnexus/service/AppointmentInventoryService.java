package com.clinicnexus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clinicnexus.dao.AppointmentInventoryDAO;
import com.clinicnexus.model.AppointmentInventory;

@Service
public class AppointmentInventoryService {
    private final AppointmentInventoryDAO appointmentInventoryDAO;

    public AppointmentInventoryService() {
        this.appointmentInventoryDAO = new AppointmentInventoryDAO();
    }

    public boolean createAppointmentInventory(AppointmentInventory appointmentInventory) {
        return appointmentInventoryDAO.addAppointmentInventory(appointmentInventory);
    }

    public AppointmentInventory getAppointmentInventory(int appointmentID, int itemID) {
        return appointmentInventoryDAO.getAppointmentInventory(appointmentID, itemID);
    }

    public boolean updateAppointmentInventory(AppointmentInventory appointmentInventory) {
        return appointmentInventoryDAO.updateAppointmentInventory(appointmentInventory);
    }

    public boolean deleteAppointmentInventory(int appointmentID, int itemID) {
        return appointmentInventoryDAO.deleteAppointmentInventory(appointmentID, itemID);
    }

    public List<AppointmentInventory> listAppointmentInventory(Integer appointmentID, Integer itemID) {
        return appointmentInventoryDAO.getAppointmentInventoryList(appointmentID, itemID);
    }

    public List<AppointmentInventory> getInventoryUsageByAppointment(int appointmentID) {
        return appointmentInventoryDAO.getInventoryUsageByAppointment(appointmentID);
    }
} 