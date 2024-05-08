package com.longcovidspa.backend.services;

import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.model.HeartRateDTO;

import java.util.Date;
import java.util.List;

public interface HealthDataService {

    List<HealthData> saveHealthDataForUser(String username, HealthData healthData);
    List<HeartRateDTO> getHeartRateData(String granularity, Date start, Date end);
}
