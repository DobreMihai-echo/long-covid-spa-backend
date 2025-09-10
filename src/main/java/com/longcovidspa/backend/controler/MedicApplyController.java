package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.MedicVerificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/medic") @RequiredArgsConstructor
public class MedicApplyController {
    private final MedicVerificationService service;

    @Data public static class MedicApplyRequest {
        private String licenseNumber;
        private String issuer;
        private String workEmail;
        private List<String> docUrls;
    }

    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> apply(@AuthenticationPrincipal UserDetailsImpl current, @RequestBody MedicApplyRequest req) {
        var app = service.submit(current.getId(), req.getLicenseNumber(), req.getIssuer(), req.getWorkEmail(), req.getDocUrls());
        return ResponseEntity.accepted().body(Map.of("status", app.getStatus()));
    }

    @GetMapping("/my-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> myStatus(@AuthenticationPrincipal UserDetailsImpl current) {
        return ResponseEntity.of(service.lastForUser(current.getId()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        service.verifyWorkEmail(token);
        // Typically redirect to frontend "verified" page
        return ResponseEntity.ok(Map.of("verified", true));
    }
}
