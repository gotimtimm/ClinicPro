package com.clinicnexus;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the Medical Clinic Management System
 * Integrates all components and provides a unified interface
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.clinicnexus")
public class ClinicManagementSystem {
    
    /**
     * Main method - Entry point for the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ClinicManagementSystem.class, args);
    }
}
