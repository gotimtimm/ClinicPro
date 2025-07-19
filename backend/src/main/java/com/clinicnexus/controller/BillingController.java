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

import com.clinicnexus.model.Billing;
import com.clinicnexus.service.BillingService;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping
    public List<Billing> getAllBilling() {
        return billingService.listBilling(null, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Billing> getBillingById(@PathVariable int id) {
        Billing billing = billingService.getBilling(id);
        if (billing != null) {
            return ResponseEntity.ok(billing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Billing> createBilling(@RequestBody Billing billing) {
        int id = billingService.createBilling(billing);
        if (id > 0) {
            Billing created = billingService.getBilling(id);
            return ResponseEntity.ok(created);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Billing> updateBilling(@PathVariable int id, @RequestBody Billing billing) {
        billing.setBillingID(id);
        boolean updated = billingService.updateBilling(billing);
        if (updated) {
            return ResponseEntity.ok(billingService.getBilling(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBilling(@PathVariable int id) {
        boolean deleted = billingService.deleteBilling(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 