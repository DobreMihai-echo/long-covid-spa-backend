package com.longcovidspa.backend.dto;

import java.util.List;

public class ScoreReq {
    public String username;
    public Integer windowDays;
    public List<Sample> series;
    public static class Sample {
        public String ts;
        public Double spo2, rpm, bb, hrv, steps;
    }
}
