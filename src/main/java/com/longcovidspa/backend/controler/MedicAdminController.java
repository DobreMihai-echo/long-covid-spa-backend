package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.MedicVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/medics") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MedicAdminController {
    private final MedicVerificationService service;

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@AuthenticationPrincipal UserDetailsImpl current, @PathVariable Long id) {
        service.approve(id, current.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@AuthenticationPrincipal UserDetailsImpl current, @PathVariable Long id, @RequestBody(required=false) String reason) {
        service.reject(id, current.getId(), reason == null ? "" : reason);
        return ResponseEntity.ok().build();
    }
}

