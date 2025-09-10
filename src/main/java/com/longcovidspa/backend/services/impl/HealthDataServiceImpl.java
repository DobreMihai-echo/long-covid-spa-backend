package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.dto.TrendPoint;
import com.longcovidspa.backend.dto.TrendResponse;
import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.model.HeartRateDTO;
import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.repositories.HealthDataRepository;
import com.longcovidspa.backend.repositories.UserRepositories;
import com.longcovidspa.backend.services.HealthDataService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HealthDataServiceImpl implements HealthDataService {

    @Autowired
    HealthDataRepository repository;

    @Autowired
    UserRepositories userRepositories;

    @Override
    @Transactional
    public HealthData saveHealthDataForUser(String username, HealthData healthData) {
        Optional<User> healthDataUser = userRepositories.findByUsername(username);
        if (healthDataUser.isPresent()) {
            healthData.setUser(healthDataUser.get());
            return repository.save(healthData);
        } else {
            throw new RuntimeException("There is no username");
        }
    }

    public TrendResponse getTrends(String username, String metric, Instant start, Instant end, String granularity) {
        List<HealthData> rows = repository.findByUser_UsernameAndReceivedDateBetweenOrderByReceivedDate(
                username, Timestamp.from(start), Timestamp.from(end)
        );

        List<TrendPoint> raw = rows.stream()
                .map(h -> new TrendPoint(h.getReceivedDate(), metricValue(metric, h)))
                .filter(tp -> tp.getValue() != null)
                .collect(Collectors.toList());

        ZoneId zone = ZoneId.systemDefault();
        Map<Instant, List<Double>> buckets = new LinkedHashMap<>();
        for (TrendPoint p : raw) {
            Instant inst = p.getTs().toInstant();
            Instant key;
            if ("HOURLY".equalsIgnoreCase(granularity)) {
                LocalDateTime ldt = LocalDateTime.ofInstant(inst, zone)
                        .withMinute(0).withSecond(0).withNano(0);
                key = ldt.atZone(zone).toInstant();
            } else {
                LocalDate d = LocalDateTime.ofInstant(inst, zone).toLocalDate();
                key = d.atStartOfDay(zone).toInstant();
            }
            buckets.computeIfAbsent(key, __ -> new ArrayList<>()).add(p.getValue());
        }

        List<TrendPoint> points = buckets.entrySet().stream()
                .map(e -> new TrendPoint(
                        Timestamp.from(e.getKey()),
                        e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN)
                ))
                .sorted(Comparator.comparing(TrendPoint::getTs))
                .collect(Collectors.toList());

        Double avg = points.stream().map(TrendPoint::getValue)
                .mapToDouble(Double::doubleValue).average().isPresent()
                ? points.stream().map(TrendPoint::getValue).mapToDouble(Double::doubleValue).average().getAsDouble()
                : null;

        double variation = 0.0;
        if (points.size() >= 2) {
            double first = points.get(0).getValue();
            double last  = points.get(points.size()-1).getValue();
            if (first != 0) variation = ((last - first) / Math.abs(first)) * 100.0;
        }

        TrendResponse dto = new TrendResponse();
        dto.setUsername(username);
        dto.setMetric(metric.toLowerCase(Locale.ROOT));
        dto.setGranularity(granularity.toUpperCase(Locale.ROOT));
        dto.setAverage(avg);
        dto.setVariationPct(variation);
        dto.setPoints(points);
        return dto;
    }

    public Optional<HealthData> findLatestByPatientId(Long patientId) {
        return repository.findTopByUser_IdOrderByReceivedDateDesc(patientId);
    }


    public List<HealthData> findRangeByPatientId(Long patientId, Timestamp start, Timestamp end) {
        return repository.findByPatientAndRange(patientId, start, end);
    }

    private Double metricValue(String metric, HealthData h) {
        switch (metric.toLowerCase(Locale.ROOT)) {
            case "spo2":  return toD(h.getSpo2());
            case "hrv":   return toD(h.getHeartRateVariability());
            case "steps": return toD(h.getSteps());
            case "sleep": return toD(h.getBodyBattery()); // proxy until you add sleep
            default:      return null;
        }
    }

    private Double toD(Integer n) { return n == null ? null : n.doubleValue(); }

    public Optional<HealthData> findLatest(String username) {
        return repository.findLatest(username);
    }


    public List<HeartRateDTO> getHeartRateData(String granularity, Date start, Date end) {
        List<HeartRateDTO> heartRates = new ArrayList<>();
        Random random = new Random();

        switch (granularity.toLowerCase()) {
            case "hourly":
                // Generate 24 data points, one for each hour
                for (int i = 0; i < 24; i++) {
                    heartRates.add(new HeartRateDTO(i + ":00", 60 + BigDecimal.valueOf(random.nextDouble() * 40)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue()));
                }
                break;
            case "daily":
                // Generate 30 data points, one for each day
                for (int i = 1; i <= 30; i++) {
                    heartRates.add(new HeartRateDTO("Day " + i, 60 + random.nextDouble() * 40));
                }
                break;
            case "monthly":
                // Generate 12 data points, one for each month
                for (int i = 1; i <= 12; i++) {
                    heartRates.add(new HeartRateDTO("Month " + i, 60 + random.nextDouble() * 40));
                }
                break;
        }
        return heartRates;
    }
}
