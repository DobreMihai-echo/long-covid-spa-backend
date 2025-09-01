package com.longcovidspa.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class InsightDTO {
    private String username;
    private Date generatedAt;

    private Integer overallRisk;
    private Integer respiratoryRisk;
    private Integer fatigueRisk;
    private Integer activityRisk;

    private String summary;
    private List<String> trendNotes = new ArrayList<>();

    private Integer spo2;
    private Integer respirationsPerMinute;
    private Integer steps;
    private Integer bodyBattery;
    private Integer heartRateVariability;

    private Boolean coldStart;
    private String modelVersion;

    private List<InsightCardDTO> cards;
    private List<RecommendationDTO> recommendations;
}
