package com.longcovidspa.backend.dto;

import java.time.LocalDateTime;

public class PatientDashboardDTO {
    private Long id;
    private String code;
    private String fullName;
    private int age;
    private LocalDateTime lastSync;
    private String status;
    private int activeAlerts;
    private String lastAlert;
    private String riskLevel;

    public PatientDashboardDTO() {}

    public PatientDashboardDTO(Long id, String code, String fullName, int age, 
                              LocalDateTime lastSync, String status, int activeAlerts, 
                              String lastAlert, String riskLevel) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.age = age;
        this.lastSync = lastSync;
        this.status = status;
        this.activeAlerts = activeAlerts;
        this.lastAlert = lastAlert;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(int activeAlerts) { this.activeAlerts = activeAlerts; }

    public String getLastAlert() { return lastAlert; }
    public void setLastAlert(String lastAlert) { this.lastAlert = lastAlert; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}