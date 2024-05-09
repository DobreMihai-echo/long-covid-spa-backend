package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.model.HeartRateDTO;
import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.repositories.HealthDataRepository;
import com.longcovidspa.backend.repositories.UserRepositories;
import com.longcovidspa.backend.services.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class HealthDataServiceImpl implements HealthDataService {

    @Autowired
    HealthDataRepository repository;

    @Autowired
    UserRepositories userRepositories;

    @Override
    @Async
    public void saveHealthDataForUser(String username, HealthData healthData) {
        Optional<User> healthDataUser = userRepositories.findByUsername(username);
        if (healthDataUser.isPresent()) {
            healthData.setUser(healthDataUser.get());
            repository.save(healthData);
        } else {
            throw new RuntimeException("There is no username");
        }
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
