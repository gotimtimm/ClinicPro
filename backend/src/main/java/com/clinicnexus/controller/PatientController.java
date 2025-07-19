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

import com.clinicnexus.model.Patient;
import com.clinicnexus.service.PatientService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.listPatients(null, null, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable int id) {
        Patient patient = patientService.getPatient(id);
        if (patient != null) {
            return ResponseEntity.ok(patient);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        boolean created = patientService.createPatient(patient);
        if (created) {
            // Optionally fetch the created patient by some means
            return ResponseEntity.ok(patient);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable int id, @RequestBody Patient patient) {
        patient.setPatientID(id);
        boolean updated = patientService.updatePatient(patient);
        if (updated) {
            return ResponseEntity.ok(patientService.getPatient(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable int id) {
        boolean deleted = patientService.deletePatient(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search/{name}")
    public ResponseEntity<List<Patient>> searchPatientsByName(@PathVariable String name) {
        List<Patient> patients = patientService.searchPatientsByName(name);
        return ResponseEntity.ok(patients);
    }
    
    @GetMapping("/id/{name}")
    public ResponseEntity<Integer> getPatientIdByName(@PathVariable String name) {
        int patientId = patientService.getPatientIdByName(name);
        if (patientId != -1) {
            return ResponseEntity.ok(patientId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}