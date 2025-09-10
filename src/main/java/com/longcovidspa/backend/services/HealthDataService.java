package com.longcovidspa.backend.services;

import com.longcovidspa.backend.dto.TrendResponse;
import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.model.HeartRateDTO;
import org.springframework.scheduling.annotation.Async;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface HealthDataService {

    HealthData saveHealthDataForUser(String username, HealthData healthData);
    List<HeartRateDTO> getHeartRateData(String granularity, Date start, Date end);

    Optional<HealthData> findLatest(String username);

    TrendResponse getTrends(String username, String metric, Instant start, Instant end, String granularity);
    Optional<HealthData> findLatestByPatientId(Long patientId);

    // ✅ NEW
    List<HealthData> findRangeByPatientId(Long patientId, Timestamp start, Timestamp end);

}
