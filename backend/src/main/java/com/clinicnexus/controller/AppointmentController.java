package com.clinicnexus.controller;

import java.util.List;
import java.util.Map;

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

import com.clinicnexus.model.Appointment;
import com.clinicnexus.service.AppointmentService;
import com.clinicnexus.dto.CreateAppointmentDTO;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.listAppointments(null, null, null, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable int id) {
        Appointment appointment = appointmentService.getAppointment(id);
        if (appointment != null) {
            return ResponseEntity.ok(appointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        int id = appointmentService.createAppointment(appointment);
        if (id > 0) {
            Appointment created = appointmentService.getAppointment(id);
            return ResponseEntity.ok(created);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable int id, @RequestBody Appointment appointment) {
        appointment.setAppointmentID(id);
        boolean updated = appointmentService.updateAppointment(appointment);
        if (updated) {
            return ResponseEntity.ok(appointmentService.getAppointment(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable int id) {
        boolean deleted = appointmentService.deleteAppointment(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/with-names")
    public ResponseEntity<List<Map<String, Object>>> getAppointmentsWithNames() {
        List<Map<String, Object>> appointments = appointmentService.getAppointmentsWithNames();
        return ResponseEntity.ok(appointments);
    }
    
    @PostMapping("/with-names")
    public ResponseEntity<Map<String, Object>> createAppointmentWithNames(@RequestBody CreateAppointmentDTO appointmentDTO) {
        int id = appointmentService.createAppointmentWithNames(
            appointmentDTO.getPatientName(),
            appointmentDTO.getDoctorName(),
            appointmentDTO.getDate(),
            appointmentDTO.getTime(),
            appointmentDTO.getDuration(),
            appointmentDTO.getVisitType(),
            appointmentDTO.getStatus(),
            appointmentDTO.getNotes()
        );
        
        if (id > 0) {
            Map<String, Object> created = appointmentService.getAppointmentWithRelatedRecords(id);
            return ResponseEntity.ok(created);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}