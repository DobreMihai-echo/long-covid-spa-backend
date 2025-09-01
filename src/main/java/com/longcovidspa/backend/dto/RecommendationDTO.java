package com.longcovidspa.backend.dto;

import lombok.Data;

@Data
public class RecommendationDTO {
    private String title;        // "Prioritize Rest Today"
    private String body;         // action text
    private Integer confidence;  // 0..100
    private String rationale;    // "HRV below baseline + poor sleep"
    private String tag;          // "fatigue" | "resp" | ...
}
