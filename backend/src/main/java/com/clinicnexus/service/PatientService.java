package com.clinicnexus.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicnexus.dao.PatientDAO;
import com.clinicnexus.model.Patient;

@Service
public class PatientService {
    
    @Autowired
    private PatientDAO patientDAO;

    public boolean createPatient(Patient patient) {
        return patientDAO.addPatient(patient);
    }

    public Patient getPatient(int patientID) {
        return patientDAO.getPatient(patientID);
    }

    public boolean updatePatient(Patient patient) {
        return patientDAO.updatePatient(patient);
    }

    public boolean deletePatient(int patientID) {
        return patientDAO.deletePatient(patientID);
    }

    public List<Patient> listPatients(String nameFilter, String insuranceFilter, Boolean activeFilter) {
        return patientDAO.getPatientList(nameFilter, insuranceFilter, activeFilter);
    }

    public Map<String, Object> getPatientWithAppointments(int patientID) {
        return patientDAO.getPatientWithAppointments(patientID);
    }
    
    public List<Patient> searchPatientsByName(String name) {
        return patientDAO.searchPatientsByName(name);
    }
    
    public int getPatientIdByName(String name) {
        return patientDAO.getPatientIdByName(name);
    }
} 
