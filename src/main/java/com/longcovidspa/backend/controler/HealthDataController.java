package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.dto.InsightDTO;
import com.longcovidspa.backend.dto.TrendResponse;
import com.longcovidspa.backend.model.ConsentScope;
import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.HealthDataService;
import com.longcovidspa.backend.services.impl.PrivacyService;
import com.longcovidspa.backend.utils.InsightsAiOrchestrator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthDataController {

    private final HealthDataService service;
    private final SimpMessagingTemplate messagingTemplate;
    private final InsightsAiOrchestrator ai;

    private final PrivacyService privacyService;

    @Value("${privacy.policy.version:}") private String requiredPolicyVersion;

    public HealthDataController(HealthDataService service,
                                SimpMessagingTemplate messagingTemplate,
                                InsightsAiOrchestrator ai, PrivacyService privacyService) {
        this.service = service; this.messagingTemplate = messagingTemplate; this.ai = ai;
        this.privacyService = privacyService;
    }

    @PostMapping
    public ResponseEntity<?> saveHealthData(@RequestParam String username,
                                            @AuthenticationPrincipal UserDetailsImpl user,
                                            @RequestBody List<HealthData> healthDataList) {
        try {
            HealthData lastSaved = null;
            for (HealthData hd : healthDataList) {
                if (hd.getReceivedDate() == null) {
                    hd.setReceivedDate(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                lastSaved = service.saveHealthDataForUser(username, hd);
            }
            if (lastSaved != null) {
                messagingTemplate.convertAndSend("/queue/healthdata/" + username, lastSaved);
            }
            boolean aiOk = privacyService.hasActiveConsent(user.getId(), ConsentScope.AI_INSIGHTS, requiredPolicyVersion);
            if (aiOk) {
                ai.compute(username).ifPresent(dto ->
                        messagingTemplate.convertAndSend("/queue/insights/" + username, dto)
                );
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

    @GetMapping("/insights/{username}/latest")
    public ResponseEntity<InsightDTO> latestInsights(@PathVariable String username) {
        return ai.compute(username).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/heart-rate")
    public ResponseEntity<?> getHeartRateAverages(
            @RequestParam("granularity") String granularity,
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date start,
            @RequestParam("end")   @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date end) {
        return ResponseEntity.ok(service.getHeartRateData(granularity,start,end));
    }

    @GetMapping("/{username}/trends")
    public ResponseEntity<TrendResponse> trends(
            @PathVariable String username,
            @RequestParam String metric,                        // spo2|hrv|sleep|steps
            @RequestParam String range,                         // today|7d|30d|custom
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end,
            @RequestParam(defaultValue = "DAILY") String granularity // DAILY|HOURLY
    ) {
        Instant now = Instant.now();
        Instant from;
        switch (range.toLowerCase()) {
            case "today":
                from = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
                break;
            case "30d":
                from = now.minus(Duration.ofDays(30));
                break;
            case "custom":
                if (start == null || end == null) return ResponseEntity.badRequest().build();
                from = start.toInstant(); now = end.toInstant(); break;
            case "7d":
            default:
                from = now.minus(Duration.ofDays(7));
        }
        return ResponseEntity.ok(service.getTrends(username, metric, from, now, granularity));
    }
}
