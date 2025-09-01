package com.longcovidspa.backend.utils;

import com.longcovidspa.backend.dto.*;
import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.repositories.HealthDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class InsightsAiOrchestrator {

    private final HealthDataRepository repo;
    private final WebClient ai;
    private final long timeoutMs;

    public InsightsAiOrchestrator(HealthDataRepository repo,
                                  WebClient aiWebClient,
                                  @Value("${ai.scoring.timeout.ms:1500}") long timeoutMs) {
        this.repo = repo; this.ai = aiWebClient; this.timeoutMs = timeoutMs;
    }

    public Optional<InsightDTO> compute(String username) {
        Instant end = Instant.now(), start = end.minus(28, ChronoUnit.DAYS);
        List<HealthData> rows = repo.findWindow(username, Timestamp.from(start), Timestamp.from(end));
        if (rows == null || rows.isEmpty()) return Optional.empty();

        HealthData last = rows.get(rows.size()-1);

        Map<String,Object> payload = buildPayload(username, rows);

        Map res = null;
        try {
            res = ai.post().uri("/score")
                    .bodyValue(payload)
                    .retrieve().bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .onErrorResume(ex -> {
                        System.out.println("[AI] scorer unreachable: " + ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            System.out.println("[AI] call failed: " + e.getMessage());
            res = null;
        }

        InsightDTO dto = (res != null) ? mapFromScorer(res, last, username) : fallbackHeuristic(last, username);

        // build tiles & recs (simple rules)
        fillTiles(dto);
        fillRecs(dto);

        return Optional.of(dto);
    }

    private Map<String,Object> buildPayload(String username, List<HealthData> rows) {
        Map<String,Object> p = new HashMap<>();
        p.put("username", username);
        p.put("windowDays", 28);
        List<Map<String,Object>> series = new ArrayList<>();
        for (HealthData h: rows) {
            Map<String,Object> s = new HashMap<>();
            s.put("ts", h.getReceivedDate().toInstant().toString());
            s.put("spo2", h.getSpo2());
            s.put("rpm", h.getRespirationsPerMinute());
            s.put("bb", h.getBodyBattery());
            s.put("hrv", h.getHeartRateVariability());
            s.put("steps", h.getSteps());
            series.add(s);
        }
        p.put("series", series);
        return p;
    }

    private InsightDTO mapFromScorer(Map res, HealthData last, String username) {
        Map scores = (Map)res.get("scores");
        Map snap   = (Map)res.get("snapshot");

        InsightDTO dto = new InsightDTO();
        dto.setUsername(username);
        dto.setGeneratedAt(new Date());
        dto.setOverallRisk(num(scores,"overall"));
        dto.setRespiratoryRisk(num(scores,"respiratory"));
        dto.setFatigueRisk(num(scores,"fatigue"));
        dto.setActivityRisk(num(scores,"activity"));
        dto.setSummary((String)res.getOrDefault("summary","AI summary"));
        dto.setTrendNotes((List<String>)res.getOrDefault("trends", List.of()));
        dto.setSpo2(num(snap,"spo2"));
        dto.setRespirationsPerMinute(num(snap,"rpm"));
        dto.setHeartRateVariability(num(snap,"hrv"));
        dto.setBodyBattery(num(snap,"bb"));
        dto.setSteps(num(snap,"steps"));
        // if scorer omitted snapshot, fall back to last
        if (dto.getSpo2()==null) dto.setSpo2(last.getSpo2());
        if (dto.getRespirationsPerMinute()==null) dto.setRespirationsPerMinute(last.getRespirationsPerMinute());
        if (dto.getHeartRateVariability()==null) dto.setHeartRateVariability(last.getHeartRateVariability());
        if (dto.getBodyBattery()==null) dto.setBodyBattery(last.getBodyBattery());
        if (dto.getSteps()==null) dto.setSteps(last.getSteps());
        return dto;
    }

    private InsightDTO fallbackHeuristic(HealthData last, String username) {
        InsightDTO dto = new InsightDTO();
        dto.setUsername(username);
        dto.setGeneratedAt(new Date());
        dto.setSpo2(last.getSpo2());
        dto.setRespirationsPerMinute(last.getRespirationsPerMinute());
        dto.setHeartRateVariability(last.getHeartRateVariability());
        dto.setBodyBattery(last.getBodyBattery());
        dto.setSteps(last.getSteps());
        int resp = (last.getSpo2()!=null && last.getSpo2()<92 ? 85 : (last.getSpo2()!=null && last.getSpo2()<95 ? 60 : 20))
                + (last.getRespirationsPerMinute()!=null && last.getRespirationsPerMinute()>20 ? 20 : 0);
        int fat  = (last.getHeartRateVariability()!=null && last.getHeartRateVariability()<40 ? 70 : 20)
                + (last.getBodyBattery()!=null && last.getBodyBattery()<35 ? 20 : 0);
        int act  = (last.getSteps()!=null && last.getSteps()<2000 ? 40 : 15);
        dto.setRespiratoryRisk(Math.min(resp,100));
        dto.setFatigueRisk(Math.min(fat,100));
        dto.setActivityRisk(Math.min(act,100));
        dto.setOverallRisk((dto.getRespiratoryRisk()+dto.getFatigueRisk()+dto.getActivityRisk())/3);
        dto.setSummary("Stable — maintain gentle progress and hydration.");
        dto.setTrendNotes(List.of("Baseline-only heuristic"));
        return dto;
    }

    private void fillTiles(InsightDTO dto) {
        List<InsightCardDTO> tiles = new ArrayList<>();
        if (val(dto.getFatigueRisk()) >= 60) {
            InsightCardDTO c = new InsightCardDTO();
            c.setId("fatigue"); c.setTitle("Fatigue Risk Detected");
            c.setSeverity(val(dto.getFatigueRisk())>=80 ? "danger" : "warning");
            c.setTimeframe("Last 3 days"); c.setConfidence(dto.getFatigueRisk());
            c.setDetails("HRV decline + low body battery");
            tiles.add(c);
        }
        if (val(dto.getRespiratoryRisk()) >= 50) {
            InsightCardDTO c = new InsightCardDTO();
            c.setId("resp"); c.setTitle("SpO₂ Below Baseline");
            c.setSeverity(val(dto.getRespiratoryRisk())>=80 ? "danger" : "warning");
            c.setTimeframe("Last 24h"); c.setConfidence(dto.getRespiratoryRisk());
            c.setDetails("SpO₂ drop and elevated respirations");
            tiles.add(c);
        }
        if (val(dto.getBodyBattery()) >= 70) {
            InsightCardDTO c = new InsightCardDTO();
            c.setId("sleep"); c.setTitle("Sleep Duration Improving");
            c.setSeverity("success"); c.setTimeframe("This week");
            c.setConfidence(75); c.setDetails("Higher recovery score vs last week");
            tiles.add(c);
        }
        if (dto.getSteps()!=null) {
            InsightCardDTO c = new InsightCardDTO();
            c.setId("activity"); c.setTitle("Activity Pattern Change");
            c.setSeverity("info"); c.setTimeframe("Last 7 days");
            c.setConfidence(70); c.setDetails("Activity correlates with symptom change");
            tiles.add(c);
        }
        dto.setCards(tiles);
    }

    private void fillRecs(InsightDTO dto) {
        List<RecommendationDTO> recs = new ArrayList<>();
        if (val(dto.getFatigueRisk()) >= 60) {
            RecommendationDTO r = new RecommendationDTO();
            r.setTag("fatigue"); r.setTitle("Prioritize Rest Today");
            r.setBody("Take 2–3 rest breaks of 15 minutes; defer intense training.");
            r.setRationale("HRV below baseline + low body battery");
            r.setConfidence(dto.getFatigueRisk());
            recs.add(r);
        }
        if (val(dto.getRespiratoryRisk()) >= 60) {
            RecommendationDTO r = new RecommendationDTO();
            r.setTag("resp"); r.setTitle("Breathing & Hydration");
            r.setBody("Practice 5-minute paced breathing and increase fluids by 0.5–1L.");
            r.setRationale("SpO₂ reduced and respirations elevated");
            r.setConfidence(dto.getRespiratoryRisk());
            recs.add(r);
        }
        if (recs.isEmpty()) {
            RecommendationDTO r = new RecommendationDTO();
            r.setTag("activity"); r.setTitle("Maintain Gentle Progress");
            r.setBody("Keep steady routine; hydrate and do light activity.");
            r.setRationale("Low overall risk"); r.setConfidence(68);
            recs.add(r);
        }
        dto.setRecommendations(recs);
    }

    private static Integer num(Map m, String k){ Object v = m==null?null:m.get(k); return v instanceof Number ? ((Number)v).intValue() : null; }
    private static int val(Integer x){ return x==null?0:x; }
}

