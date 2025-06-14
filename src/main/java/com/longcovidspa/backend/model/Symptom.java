package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to associate symptoms with a user
    private Long userId;

    // When the symptom was logged
    private LocalDate date;

    // Symptom type e.g., Fatigue, Fever, etc.
    private String symptomType;

    // Numeric value indicating intensity
    private int severity;

    // Optional, for additional details
    private String notes;

    public Symptom() {}

    public Symptom(Long userId, LocalDate date, String symptomType, int severity, String notes) {
        this.userId = userId;
        this.date = date;
        this.symptomType = symptomType;
        this.severity = severity;
        this.notes = notes;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSymptomType() {
        return symptomType;
    }
    public void setSymptomType(String symptomType) {
        this.symptomType = symptomType;
    }

    public int getSeverity() {
        return severity;
    }
    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}