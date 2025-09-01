package com.longcovidspa.backend.dto;

import java.util.List;

public class TrendResponse {
    private String username;
    private String metric;        // spo2 | hrv | sleep | steps
    private String granularity;   // DAILY | HOURLY
    private Double average;
    private Double variationPct;
    private List<TrendPoint> points;

    public TrendResponse() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
    public String getGranularity() { return granularity; }
    public void setGranularity(String granularity) { this.granularity = granularity; }
    public Double getAverage() { return average; }
    public void setAverage(Double average) { this.average = average; }
    public Double getVariationPct() { return variationPct; }
    public void setVariationPct(Double variationPct) { this.variationPct = variationPct; }
    public List<TrendPoint> getPoints() { return points; }
    public void setPoints(List<TrendPoint> points) { this.points = points; }
}
