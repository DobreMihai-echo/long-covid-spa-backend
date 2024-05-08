package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name="user_health_data")
@Getter
@Setter
public class HealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "heart_rate_variability")
    private Integer heartRateVariability;
    private Integer spo2;
    private Integer steps;
    @Column(name = "respirations_per_minute")
    private Integer respirationsPerMinute;
    private Integer distance;
    private Integer calories;
    @Column(name = "body_battery")
    private Integer bodyBattery;
    @Column(name = "received_date")
    private Timestamp receivedDate;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
}
