package com.longcovidspa.backend.controler;


import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.services.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/health")
public class HealthDataController {

    @Autowired
    private HealthDataService service;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    private ResponseEntity<?> saveHealthData(@RequestParam String username,
                                             @RequestBody List<HealthData> healthDataList) {
        try {
            HealthData lastSaved = null;
            for (HealthData hd: healthDataList) {
                lastSaved = service.saveHealthDataForUser(username, hd);
            }
            if (lastSaved != null) {
                messagingTemplate.convertAndSend("/queue/healthdata/" + username, lastSaved);
            }
            return ResponseEntity.ok("Successfully updated");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("There was a problem saving the data " + e.getMessage());
        }
    }

    @GetMapping("/{username}/latest")
    public ResponseEntity<HealthData> latest(@PathVariable String username) {
        return ResponseEntity.of(service.findLatest(username));
    }

    @GetMapping("/heart-rate")
    public ResponseEntity<?> getHeartRateAverages(
            @RequestParam("granularity") String granularity,
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date end) {
        return ResponseEntity.ok(service.getHeartRateData(granularity,start,end));
        }
}
