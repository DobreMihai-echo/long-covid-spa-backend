package com.longcovidspa.backend.controler;


import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.services.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("api/health")
public class HealthDataController {

    @Autowired
    private HealthDataService service;


    @PostMapping
    private ResponseEntity<?> saveHealthData(@RequestParam(name = "username") String username, @RequestBody HealthData healthData) {
        System.out.println("CONTROLLER");
        try {
            service.saveHealthDataForUser(username, healthData);
            return ResponseEntity.ok("Successfully updated");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("There was a problem saving the data" + e.getMessage());
        }
    }

    @GetMapping("/heart-rate")
    public ResponseEntity<?> getHeartRateAverages(
            @RequestParam("granularity") String granularity,
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date end) {
        return ResponseEntity.ok(service.getHeartRateData(granularity,start,end));
        }
}
