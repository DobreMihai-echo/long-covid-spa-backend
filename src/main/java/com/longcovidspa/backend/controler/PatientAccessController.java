package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.model.ConsentScope;
import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.HealthDataService;
import com.longcovidspa.backend.repositories.PatientAssignmentRepo;
import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.services.impl.PrivacyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/access") @RequiredArgsConstructor
public class PatientAccessController {
    private final PatientAssignmentRepo assignRepo;
    private final PrivacyService privacy;
    private final HealthDataService health;

    private static final String POLICY_VERSION = "2025-09-01";

    // Medic requests access to a patient (you can implement a patient approval workflow)
    @Data static class AssignRequest { private Long patientId; }
    @PostMapping("/request")
    @PreAuthorize("hasRole('MEDIC')")
    public ResponseEntity<?> request(@AuthenticationPrincipal UserDetailsImpl current, @RequestBody AssignRequest req) {
        // For brevity, auto-grant. In production, create a request and require patient approval.
        var pa = new com.longcovidspa.backend.model.PatientAssignment();
        pa.setMedicId(current.getId());
        pa.setPatientId(req.getPatientId());
        assignRepo.save(pa);
        return ResponseEntity.ok().build();
    }

    // Example gated endpoint: medic reads patient's latest data
    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasRole('MEDIC')")
    public ResponseEntity<HealthData> latest(@AuthenticationPrincipal UserDetailsImpl current, @PathVariable Long patientId) {
        Long medicId = current.getId();
        boolean ok = assignRepo.existsActive(medicId, patientId)
                && privacy.hasActiveConsent(patientId, ConsentScope.PROVIDER_SHARING, POLICY_VERSION);
        if (!ok) return ResponseEntity.status(403).build();
        return ResponseEntity.of(health.findLatestByPatientId(patientId));
    }
}

