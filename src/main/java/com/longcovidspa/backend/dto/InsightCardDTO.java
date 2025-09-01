package com.longcovidspa.backend.dto;

import lombok.Data;

@Data
public class InsightCardDTO {
    private String id;           // "fatigue", "resp", "sleep", "activity"
    private String title;        // "Fatigue Risk Detected"
    private String severity;     // "danger" | "warning" | "success" | "info"
    private String timeframe;    // "Last 3 days"
    private Integer confidence;  // 0..100
    private String details;      // short one-liner for the tile
}
