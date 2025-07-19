package com.clinicnexus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clinicnexus.dao.BillingDAO;
import com.clinicnexus.model.Billing;

@Service
public class BillingService {
    private final BillingDAO billingDAO;

    public BillingService() {
        this.billingDAO = new BillingDAO();
    }

    public int createBilling(Billing billing) {
        return billingDAO.addBilling(billing);
    }

    public Billing getBilling(int billingID) {
        return billingDAO.getBilling(billingID);
    }

    public boolean updateBilling(Billing billing) {
        return billingDAO.updateBilling(billing);
    }

    public boolean deleteBilling(int billingID) {
        return billingDAO.deleteBilling(billingID);
    }

    public List<Billing> listBilling(Integer appointmentID, Boolean paidFilter) {
        return billingDAO.getBillingList(appointmentID, paidFilter);
    }

    public Billing getBillingByAppointment(int appointmentID) {
        return billingDAO.getBillingByAppointment(appointmentID);
    }
} 