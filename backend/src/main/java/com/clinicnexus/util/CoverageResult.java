package com.clinicnexus.util;

public class CoverageResult {
    private final boolean hasMinimumCoverage;
    private final String message;
    private final int doctorCount;
    private final int nurseCount;
    private final int adminCount;
    
    public CoverageResult(boolean hasMinimumCoverage, String message, int doctorCount, int nurseCount, int adminCount) {
        this.hasMinimumCoverage = hasMinimumCoverage;
        this.message = message;
        this.doctorCount = doctorCount;
        this.nurseCount = nurseCount;
        this.adminCount = adminCount;
    }
    
    public boolean hasMinimumCoverage() { return hasMinimumCoverage; }
    public String getMessage() { return message; }
    public int getDoctorCount() { return doctorCount; }
    public int getNurseCount() { return nurseCount; }
    public int getAdminCount() { return adminCount; }
}