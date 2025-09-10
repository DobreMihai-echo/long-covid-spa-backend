package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.model.MedicApplication;
import com.longcovidspa.backend.model.UserLite;
import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.MedicVerificationService;
import com.longcovidspa.backend.services.impl.PatientsAssignmentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/medics")
@RequiredArgsConstructor
public class AdminMedicController {

    private final MedicVerificationService service;
    public record RejectBody(String reason) {}

    private final PatientsAssignmentServiceImpl patientService;

    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MedicApplication>> list(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(status, PageRequest.of(page, size)));
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicApplication> get(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping("/applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicApplication> approve(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long adminId = currentUser.getId();
        return ResponseEntity.ok(service.approve(id, adminId));
    }

    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicApplication> reject(@PathVariable Long id,
                                                   @RequestBody RejectBody body, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long adminId = currentUser.getId();
        return ResponseEntity.ok(service.reject(id, adminId, body == null ? null : body.reason));
    }

    // --- LIST patients of a medic ---
    @GetMapping("/{medicId}/patients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserLite>> listPatientsOfDoctor(
            @PathVariable Long medicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(patientService.listPatientsOfDoctor(medicId, PageRequest.of(page, size)));
    }

    // --- Search assignable patients (ROLE_USER) ---
    @GetMapping("/patients/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserLite>> searchPatients(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(patientService.searchAssignablePatients(q, PageRequest.of(page, size)));
    }

    // --- Assign patient to medic ---
    @PostMapping("/{medicId}/patients/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignPatient(
            @PathVariable Long medicId,
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        patientService.assignPatient(medicId, patientId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // --- Unassign patient from medic ---
    @DeleteMapping("/{medicId}/patients/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignPatient(
            @PathVariable Long medicId,
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        patientService.unassignPatient(medicId, patientId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
