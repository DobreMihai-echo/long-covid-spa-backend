package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.model.ConsentRecord;
import com.longcovidspa.backend.model.ConsentScope;
import com.longcovidspa.backend.model.ExportJob;
import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.impl.PrivacyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("api/privacy")
@RequiredArgsConstructor
public class PrivacyController {

    private final PrivacyService svc;

    @GetMapping("/consents")
    public List<ConsentRecord> getConsents(@AuthenticationPrincipal UserDetailsImpl principal){
        return svc.listConsents(principal.getId());
    }

    @PostMapping("/consents")
    public ConsentRecord postConsent(@AuthenticationPrincipal UserDetailsImpl principal, @RequestBody ConsentRequest body){
        if (Boolean.TRUE.equals(body.withdraw)) {
            return svc.withdrawConsent(principal.getId());
        }
        return svc.giveConsent(principal.getId(), body.scopes, body.policyVersion, body.textHash);
    }

    @Data
    public static class ConsentRequest {
        public Set<ConsentScope> scopes;
        public String policyVersion;
        public String textHash;
        public Boolean withdraw;
    }

    // ----- EXPORT -----
    @PostMapping("/export")
    public Map<String,String> createExport(@AuthenticationPrincipal UserDetailsImpl principal){
        var job = svc.createExportJob(principal.getId());
        svc.runExportJob(job.getId());
        return Map.of("jobId", job.getId());
    }

    @GetMapping("/export/{jobId}")
    public ResponseEntity<?> getExportJob(@PathVariable String jobId){
        var job = svc.getExportJob(jobId).orElse(null);
        if (job==null) return ResponseEntity.notFound().build();
        var res = new LinkedHashMap<String,Object>();
        res.put("status", job.getStatus());
        res.put("readyAt", job.getReadyAt());
        if (job.getStatus()== ExportJob.Status.READY) {
            res.put("downloadUrl", String.format("/api/privacy/export/%s/download?token=%s", job.getId(), job.getDownloadToken()));
            res.put("expiresAt", job.getExpiresAt());
        }
        if (job.getStatus()== ExportJob.Status.FAILED) res.put("error", job.getErrorMsg());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/export/{jobId}/download")
    public ResponseEntity<?> download(@PathVariable String jobId, @RequestParam String token) throws Exception {
        var fOpt = svc.resolveDownload(jobId, token);
        if (fOpt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired");
        File f = fOpt.get();
        byte[] bytes = Files.readAllBytes(f.toPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    // ----- ERASURE -----
    @PostMapping("/erase")
    public Map<String,Object> requestErase(@AuthenticationPrincipal UserDetailsImpl principal){
        var req = svc.requestErasure(principal.getId());
        svc.processErasure(req.getId());
        return Map.of("requestId", req.getId(), "status", req.getStatus(), "requestedAt", req.getRequestedAt());
    }

    @GetMapping("/consents/latest")
    public ResponseEntity<ConsentRecord> latest(@AuthenticationPrincipal UserDetailsImpl principal) {
        var uId = principal.getId();
        return ResponseEntity.of(svc.latestConsent(uId));
    }
}
