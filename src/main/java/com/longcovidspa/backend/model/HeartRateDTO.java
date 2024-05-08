package com.longcovidspa.backend.model;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HeartRateDTO {
    private String time;
    private Double value;

    // Constructor
    public HeartRateDTO(String time, Double value) {
        this.time = time;
        this.value = value;
    }

    // Getters and setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        if (value != null) {
            this.value = BigDecimal.valueOf(value)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            this.value = null;
        }
    }
}
