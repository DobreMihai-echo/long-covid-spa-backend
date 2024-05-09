package com.longcovidspa.backend.services;

import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.model.HeartRateDTO;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;
import java.util.List;

public interface HealthDataService {

    @Async
    void saveHealthDataForUser(String username, HealthData healthData);
    List<HeartRateDTO> getHeartRateData(String granularity, Date start, Date end);
}
