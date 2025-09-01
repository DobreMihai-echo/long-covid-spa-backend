package com.longcovidspa.backend.dto;

import java.time.Instant;

public record HealthDataDTO(
        Long id,
        Integer heartRateVariability,
        Integer spo2,
        Integer steps,
        Integer respirationsPerMinute,
        Integer distance,
        Integer calories,
        Integer bodyBattery,
        Instant receivedDate
){}
