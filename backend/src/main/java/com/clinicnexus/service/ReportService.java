package com.clinicnexus.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicnexus.config.DatabaseConnection;

/**
 * Service for generating all reports as specified in the project requirements
 */
@Service
public class ReportService {
    
    /**
     * Generate Patient Visit Analysis Report (Report 5.1)
     * Group Member 1 - Patient Records and Appointment Records
     * @param year Year for the report
     * @param month Month for the report
     * @return List of patient visit analysis data
     */
    public List<Map<String, Object>> generatePatientVisitAnalysis(int year, int month) {
        String sql = "SELECT " +
                    "p.PatientID, p.Name as PatientName, " +
                    "YEAR(CURDATE()) - YEAR(p.BirthDate) as Age, " +
                    "WEEK(a.Date) as WeekNumber, " +
                    "COUNT(a.AppointmentID) as VisitCount, " +
                    "GROUP_CONCAT(DISTINCT a.VisitType) as VisitTypes " +
                    "FROM Patient p " +
                    "JOIN Appointment a ON p.PatientID = a.PatientID " +
                    "WHERE YEAR(a.Date) = ? AND MONTH(a.Date) = ? " +
                    "AND a.Status = 'Done' " +
                    "GROUP BY p.PatientID, WEEK(a.Date) " +
                    "ORDER BY p.Name, WeekNumber";
        
        List<Map<String, Object>> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("patientID", rs.getInt("PatientID"));
                report.put("patientName", rs.getString("PatientName"));
                report.put("age", rs.getInt("Age"));
                report.put("weekNumber", rs.getInt("WeekNumber"));
                report.put("visitCount", rs.getInt("VisitCount"));
                report.put("visitTypes", rs.getString("VisitTypes"));
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("Error generating patient visit analysis: " + e.getMessage());
        }
        
        return reports;
    }
    
    /**
     * Generate Doctor Performance Metrics (Report 5.2)
     * Group Member 2 - Doctor Records, Appointment Records, and Patient Feedback
     * @param year Year for the report
     * @param quarter Quarter (1-4) for the report
     * @return List of doctor performance data
     */
    public List<Map<String, Object>> generateDoctorPerformanceMetrics(int year, int quarter) {
        // Calculate month range for quarter
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = quarter * 3;
        
        String sql = "SELECT " +
                    "s.StaffID, s.Name as DoctorName, s.Specialization, " +
                    "COUNT(a.AppointmentID) as TotalAppointments, " +
                    "SUM(CASE WHEN a.Status = 'Done' THEN 1 ELSE 0 END) as CompletedAppointments, " +
                    "SUM(CASE WHEN a.Status = 'Canceled' THEN 1 ELSE 0 END) as CanceledAppointments, " +
                    "COUNT(DISTINCT a.PatientID) as UniquePatients, " +
                    "AVG(CASE WHEN f.Rating IS NOT NULL THEN f.Rating ELSE 0 END) as AverageRating, " +
                    "COUNT(f.FeedbackID) as TotalFeedbacks, " +
                    "SUM(b.Amount) as TotalRevenue " +
                    "FROM Staff s " +
                    "LEFT JOIN Appointment a ON s.StaffID = a.DoctorID " +
                    "LEFT JOIN Feedback f ON a.AppointmentID = f.AppointmentID " +
                    "LEFT JOIN Billing b ON a.AppointmentID = b.AppointmentID " +
                    "WHERE s.JobType = 'Doctor' AND s.ActiveStatus = true " +
                    "AND YEAR(a.Date) = ? AND MONTH(a.Date) BETWEEN ? AND ? " +
                    "GROUP BY s.StaffID, s.Name, s.Specialization " +
                    "ORDER BY TotalAppointments DESC";
        
        List<Map<String, Object>> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, startMonth);
            pstmt.setInt(3, endMonth);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("doctorID", rs.getInt("StaffID"));
                report.put("doctorName", rs.getString("DoctorName"));
                report.put("specialization", rs.getString("Specialization"));
                report.put("totalAppointments", rs.getInt("TotalAppointments"));
                report.put("completedAppointments", rs.getInt("CompletedAppointments"));
                report.put("canceledAppointments", rs.getInt("CanceledAppointments"));
                report.put("uniquePatients", rs.getInt("UniquePatients"));
                report.put("averageRating", rs.getDouble("AverageRating"));
                report.put("totalFeedbacks", rs.getInt("TotalFeedbacks"));
                report.put("totalRevenue", rs.getDouble("TotalRevenue"));
                
                // Calculate success rate
                int total = rs.getInt("TotalAppointments");
                int completed = rs.getInt("CompletedAppointments");
                double successRate = total > 0 ? (double) completed / total * 100 : 0;
                report.put("successRate", successRate);
                
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("Error generating doctor performance metrics: " + e.getMessage());
        }
        
        return reports;
    }
    
    /**
     * Generate Financial Operations Report (Report 5.3)
     * Group Member 3 - Appointment Records and Billing Transactions
     * @param year Year for the report
     * @param month Month for the report
     * @return List of financial operations data
     */
    public List<Map<String, Object>> generateFinancialOperationsReport(int year, int month) {
        String sql = "SELECT " +
                    "DATE(a.Date) as ReportDate, " +
                    "COUNT(b.BillingID) as TotalBills, " +
                    "SUM(b.Amount) as TotalRevenue, " +
                    "AVG(b.Amount) as AverageRevenue, " +
                    "SUM(CASE WHEN b.Paid = true THEN b.Amount ELSE 0 END) as PaidRevenue, " +
                    "SUM(CASE WHEN b.Paid = false THEN b.Amount ELSE 0 END) as UnpaidRevenue, " +
                    "COUNT(CASE WHEN b.Paid = true THEN 1 END) as PaidBills, " +
                    "COUNT(CASE WHEN b.Paid = false THEN 1 END) as UnpaidBills " +
                    "FROM Appointment a " +
                    "JOIN Billing b ON a.AppointmentID = b.AppointmentID " +
                    "WHERE YEAR(a.Date) = ? AND MONTH(a.Date) = ? " +
                    "GROUP BY DATE(a.Date) " +
                    "ORDER BY ReportDate";
        
        List<Map<String, Object>> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportDate", rs.getDate("ReportDate"));
                report.put("totalBills", rs.getInt("TotalBills"));
                report.put("totalRevenue", rs.getDouble("TotalRevenue"));
                report.put("averageRevenue", rs.getDouble("AverageRevenue"));
                report.put("paidRevenue", rs.getDouble("PaidRevenue"));
                report.put("unpaidRevenue", rs.getDouble("UnpaidRevenue"));
                report.put("paidBills", rs.getInt("PaidBills"));
                report.put("unpaidBills", rs.getInt("UnpaidBills"));
                
                // Calculate payment rate
                int totalBills = rs.getInt("TotalBills");
                int paidBills = rs.getInt("PaidBills");
                double paymentRate = totalBills > 0 ? (double) paidBills / totalBills * 100 : 0;
                report.put("paymentRate", paymentRate);
                
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("Error generating financial operations report: " + e.getMessage());
        }
        
        return reports;
    }
    
    /**
     * Generate Resource Utilization Report (Report 5.4)
     * Group Member 4 - Inventory Records and Appointment Records
     * @param year Year for the report
     * @return List of resource utilization data
     */
    public List<Map<String, Object>> generateResourceUtilizationReport(int year) {
        String sql = "SELECT " +
                    "i.ItemID, i.Name as ItemName, i.Type as ItemType, " +
                    "i.Purpose, i.StockQuantity as CurrentStock, " +
                    "i.ReorderThreshold, i.UnitPrice, " +
                    "COALESCE(SUM(ai.QuantityUsed), 0) as TotalUsed, " +
                    "COUNT(DISTINCT ai.AppointmentID) as AppointmentsUsed, " +
                    "COALESCE(SUM(ai.QuantityUsed), 0) * i.UnitPrice as TotalCost " +
                    "FROM Inventory i " +
                    "LEFT JOIN Appointment_Inventory ai ON i.ItemID = ai.ItemID " +
                    "LEFT JOIN Appointment a ON ai.AppointmentID = a.AppointmentID " +
                    "WHERE i.ActiveStatus = true " +
                    "AND (YEAR(a.Date) = ? OR a.Date IS NULL) " +
                    "GROUP BY i.ItemID, i.Name, i.Type, i.Purpose, i.StockQuantity, i.ReorderThreshold, i.UnitPrice " +
                    "ORDER BY TotalUsed DESC, i.Name";
        
        List<Map<String, Object>> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("itemID", rs.getInt("ItemID"));
                report.put("itemName", rs.getString("ItemName"));
                report.put("itemType", rs.getString("ItemType"));
                report.put("purpose", rs.getString("Purpose"));
                report.put("currentStock", rs.getInt("CurrentStock"));
                report.put("reorderThreshold", rs.getInt("ReorderThreshold"));
                report.put("unitPrice", rs.getDouble("UnitPrice"));
                report.put("totalUsed", rs.getInt("TotalUsed"));
                report.put("appointmentsUsed", rs.getInt("AppointmentsUsed"));
                report.put("totalCost", rs.getDouble("TotalCost"));
                
                // Calculate utilization metrics
                int currentStock = rs.getInt("CurrentStock");
                int totalUsed = rs.getInt("TotalUsed");
                int reorderThreshold = rs.getInt("ReorderThreshold");
                
                // Usage rate (monthly average)
                double monthlyUsageRate = totalUsed > 0 ? (double) totalUsed / 12 : 0;
                report.put("monthlyUsageRate", monthlyUsageRate);
                
                // Stock status
                String stockStatus;
                if (currentStock <= reorderThreshold) {
                    stockStatus = "Low Stock";
                } else if (currentStock <= reorderThreshold * 2) {
                    stockStatus = "Medium Stock";
                } else {
                    stockStatus = "High Stock";
                }
                report.put("stockStatus", stockStatus);
                
                // Equipment utilization frequency (for equipment items)
                if ("Equipment".equals(rs.getString("ItemType"))) {
                    int appointmentsUsed = rs.getInt("AppointmentsUsed");
                    double utilizationFrequency = appointmentsUsed > 0 ? (double) appointmentsUsed / 52 : 0; // Weekly average
                    report.put("utilizationFrequency", utilizationFrequency);
                }
                
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("Error generating resource utilization report: " + e.getMessage());
        }
        
        return reports;
    }
    /**
     * Generate monthly summary report
     */
    public Map<String, Object> generateMonthlySummaryReport(int year, int month) {
        Map<String, Object> summary = new HashMap<>();
        
        // Add summary data
        summary.put("year", year);
        summary.put("month", month);
        summary.put("totalAppointments", 0);
        summary.put("totalRevenue", 0.0);
        summary.put("totalPatients", 0);
        
        // You can add more detailed implementation here
        return summary;
    }

    /**
     * Export report data to CSV file
     */
    public boolean exportReportToCSV(List<Map<String, Object>> data, String filename) {
        try {
            // Add your CSV export logic here
            System.out.println("Exporting " + data.size() + " records to " + filename);
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
            return false;
        }
    }
} 
