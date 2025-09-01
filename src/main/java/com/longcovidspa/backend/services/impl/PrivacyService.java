package com.longcovidspa.backend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.longcovidspa.backend.model.*;
import com.longcovidspa.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class PrivacyService {

    private final ConsentRecordRepository consentRepo;
    private final ExportJobRepository exportRepo;
    private final ErasureRequestRepository eraseRepo;
    private final AuditService audit;
    private final UserRepositories userRepo;
    private final HealthDataRepository healthRepo;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${privacy.export.retention.days:7}")
    private int exportRetentionDays;

    public List<ConsentRecord> listConsents(Long userId) {
        return consentRepo.findByUserIdOrderByGrantedAtDesc(userId);
    }

    public ConsentRecord giveConsent(Long userId, Set<ConsentScope> scopes, String version, String textHash) {
        var rec = ConsentRecord.builder()
                .userId(userId).scopes(scopes).policyVersion(version).textHash(textHash).grantedAt(Instant.now())
                .build();
        var saved = consentRepo.save(rec);
        audit.log(userId, "CONSENT_GIVEN", "consent:" + version, null, null);
        return saved;
    }

    public ConsentRecord withdrawConsent(Long userId) {
        var list = consentRepo.findByUserIdOrderByGrantedAtDesc(userId);
        if (list.isEmpty()) return null;
        var current = list.get(0);
        if (current.getWithdrawnAt() == null) {
            current.setWithdrawnAt(Instant.now());
            consentRepo.save(current);
            audit.log(userId, "CONSENT_WITHDRAWN", "consent:" + current.getPolicyVersion(), null, null);
        }
        return current;
    }

    public ExportJob createExportJob(Long userId) {
        var job = ExportJob.builder()
                .id(UUID.randomUUID().toString()).userId(userId)
                .status(ExportJob.Status.PENDING)
                .createdAt(Instant.now())
                .build();
        exportRepo.save(job);
        audit.log(userId, "EXPORT_REQUESTED", "export:" + job.getId(), null, null);
        return job;
    }

    @Async("privacyExecutor")
    public void runExportJob(String jobId) {
        var job = exportRepo.findById(jobId).orElseThrow();
        try {
            var user = userRepo.findById(job.getUserId()).orElseThrow();
            var measurements = healthRepo.findAll().stream()
                    .filter(h -> h.getUser() != null && Objects.equals(h.getUser().getId(), job.getUserId()))
                    .toList();

            Path tempDir = Files.createTempDirectory("export-");
            // JSON
            var json = Map.of("user", scrubUser(user), "healthData", measurements);
            Path jsonFile = tempDir.resolve("data.json");
            om.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), json);

            // CSV
            Path csvFile = tempDir.resolve("health_data.csv");
            try (var writer = Files.newBufferedWriter(csvFile);
                 var csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("id", "receivedDate", "spo2", "hrv", "respirations", "steps", "distance", "calories", "bodyBattery"))) {
                for (HealthData h : measurements) {
                    csv.printRecord(
                            h.getId(), h.getReceivedDate(), h.getSpo2(),
                            h.getHeartRateVariability(), h.getRespirationsPerMinute(),
                            h.getSteps(), h.getDistance(), h.getCalories(), h.getBodyBattery()
                    );
                }
            }

            // ZIP
            Path zip = tempDir.resolve("export.zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
                zos.putNextEntry(new ZipEntry("data.json"));
                Files.copy(jsonFile, zos);
                zos.closeEntry();
                zos.putNextEntry(new ZipEntry("health_data.csv"));
                Files.copy(csvFile, zos);
                zos.closeEntry();
            }

            job.setFilePath(zip.toAbsolutePath().toString());
            job.setStatus(ExportJob.Status.READY);
            job.setReadyAt(Instant.now());
            job.setExpiresAt(Instant.now().plus(exportRetentionDays, ChronoUnit.DAYS));
            job.setDownloadToken(UUID.randomUUID().toString());
            exportRepo.save(job);

        } catch (Exception e) {
            job.setStatus(ExportJob.Status.FAILED);
            job.setErrorMsg(e.getMessage());
            exportRepo.save(job);
        }
    }

    private Map<String, Object> scrubUser(User u) {
        // Return only user-visible fields; avoid secrets
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "displayName", u.getFirstName() + " " + u.getLastName()
        );
    }
    public Optional<ExportJob> getExportJob(String id){ return exportRepo.findById(id); }

    public Optional<File> resolveDownload(String id, String token) {
        var job = exportRepo.findById(id).orElse(null);
        if (job==null || job.getStatus()!= ExportJob.Status.READY) return Optional.empty();
        if (job.getExpiresAt()!=null && Instant.now().isAfter(job.getExpiresAt())) return Optional.empty();
        if (!Objects.equals(job.getDownloadToken(), token)) return Optional.empty();
        return Optional.ofNullable(job.getFilePath()).map(File::new).filter(File::exists);
    }

    public ErasureRequest requestErasure(Long userId) {
        var req = eraseRepo.save(ErasureRequest.builder()
                .userId(userId).status(ErasureRequest.Status.REQUESTED).requestedAt(Instant.now()).build());
        audit.log(userId, "ERASURE_REQUESTED", "erasure:"+req.getId(), null, null);
        return req;
    }

    @Async("privacyExecutor")
    public void processErasure(Long reqId) {
        var req = eraseRepo.findById(reqId).orElseThrow();
        try {
            req.setStatus(ErasureRequest.Status.IN_PROGRESS);
            eraseRepo.save(req);

            // delete health data
            var all = healthRepo.findAll().stream()
                    .filter(h -> h.getUser()!=null && Objects.equals(h.getUser().getId(), req.getUserId())).toList();
            healthRepo.deleteAll(all);

            // TODO: optionally anonymize User instead of delete if you need audit trail
            // userRepo.deleteById(req.getUserId());

            req.setStatus(ErasureRequest.Status.DONE);
            req.setExecutedAt(Instant.now());
            eraseRepo.save(req);
            audit.log(req.getUserId(), "ERASURE_DONE", "erasure:"+req.getId(), null, null);

        } catch (Exception e){
            req.setStatus(ErasureRequest.Status.FAILED);
            req.setErrorMsg(e.getMessage());
            eraseRepo.save(req);
        }
    }

    public Optional<ConsentRecord> latestConsent(Long userId) {
        return listConsents(userId).stream()
                .sorted(Comparator.comparing(ConsentRecord::getGrantedAt).reversed())
                .findFirst();
    }

    // require specific scope, active (not withdrawn), and optionally a required policy version
    public boolean hasActiveConsent(Long userId, ConsentScope scope, String requiredVersionOrNull) {
        var latest = latestConsent(userId).orElse(null);
        if (latest == null) return false;
        if (latest.getWithdrawnAt() != null) return false;
        if (requiredVersionOrNull != null && !requiredVersionOrNull.equals(latest.getPolicyVersion())) return false;
        return latest.getScopes() != null && latest.getScopes().contains(scope);
    }
}
