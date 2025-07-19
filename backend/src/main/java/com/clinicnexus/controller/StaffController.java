package com.clinicnexus.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicnexus.model.Staff;
import com.clinicnexus.service.StaffService;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public List<Staff> getAllStaff() {
        return staffService.listStaff(null, null, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable int id) {
        Staff staff = staffService.getStaff(id);
        if (staff != null) {
            return ResponseEntity.ok(staff);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Staff> createStaff(@RequestBody Staff staff) {
        boolean created = staffService.createStaff(staff);
        if (created) {
            return ResponseEntity.ok(staff);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(@PathVariable int id, @RequestBody Staff staff) {
        staff.setStaffID(id);
        boolean updated = staffService.updateStaff(staff);
        if (updated) {
            return ResponseEntity.ok(staffService.getStaff(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable int id) {
        boolean deleted = staffService.deleteStaff(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search/{name}")
    public ResponseEntity<List<Staff>> searchStaffByName(@PathVariable String name) {
        List<Staff> staff = staffService.searchStaffByName(name);
        return ResponseEntity.ok(staff);
    }
    
    @GetMapping("/id/{name}")
    public ResponseEntity<Integer> getStaffIdByName(@PathVariable String name) {
        int staffId = staffService.getStaffIdByName(name);
        if (staffId != -1) {
            return ResponseEntity.ok(staffId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}