package com.longcovidspa.backend.dto;

import java.sql.Timestamp;

public class TrendPoint {
    private Timestamp ts;
    private Double value;

    public TrendPoint() {}
    public TrendPoint(Timestamp ts, Double value) { this.ts = ts; this.value = value; }

    public Timestamp getTs() { return ts; }
    public void setTs(Timestamp ts) { this.ts = ts; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
