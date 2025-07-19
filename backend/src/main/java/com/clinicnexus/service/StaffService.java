package com.clinicnexus.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.dao.StaffDAO;
import com.clinicnexus.model.Staff;

@Service
public class StaffService {
    private final StaffDAO staffDAO;

    public StaffService() {
        this.staffDAO = new StaffDAO();
    }

    public boolean createStaff(Staff staff) {
        return staffDAO.addStaff(staff);
    }

    public Staff getStaff(int staffID) {
        return staffDAO.getStaff(staffID);
    }

    public boolean updateStaff(Staff staff) {
        return staffDAO.updateStaff(staff);
    }

    public boolean deleteStaff(int staffID) {
        return staffDAO.deleteStaff(staffID);
    }

    public List<Staff> listStaff(String jobTypeFilter, String specializationFilter, Boolean activeFilter) {
        return staffDAO.getStaffList(jobTypeFilter, specializationFilter, activeFilter);
    }

    public Map<String, Object> getStaffWithAppointments(int staffID) {
        return staffDAO.getStaffWithAppointments(staffID);
    }
    
    public List<Staff> searchStaffByName(String name) {
        return staffDAO.searchStaffByName(name);
    }
    
    public int getStaffIdByName(String name) {
        return staffDAO.getStaffIdByName(name);
    }
} 
