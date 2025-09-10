package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.model.MedicApplication;
import com.longcovidspa.backend.repositories.MedicApplicationRepo;
import com.longcovidspa.backend.repositories.UserRepositories;
import com.longcovidspa.backend.services.MedicVerificationService;
import com.longcovidspa.backend.utils.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicVerificationServiceImpl implements MedicVerificationService {

    private final MedicApplicationRepo appRepo;
    private final UserRepositories userRepo;
    private final RoleService roleService;
    private final EmailService email;
    private final SimpMessagingTemplate messaging; // optional WS notify to admins

    @Value("${app.frontend.base-url}")
    private String frontendBase;

    @Value("${app.medic.allowed-domains:yahoo.com}")
    private String allowedDomainsCsv;

    private boolean domainAllowed(String email) {
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        for (String d : allowedDomainsCsv.split(",")) {
            if (domain.equals(d.trim().toLowerCase())) return true;
        }
        return false;
    }

    @Override
    public Page<MedicApplication> list(String status, Pageable pageable) {
        String st = status == null ? "PENDING" : status.toUpperCase();
        return appRepo.findByStatusOrderBySubmittedAtDesc(st, pageable);
    }

    @Override
    public MedicApplication get(Long id) {
        return appRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    @Transactional
    @Override
    public MedicApplication submit(Long userId, String licenseNo, String issuer, String workEmail, List<String> docUrls) {
        if (appRepo.existsByUserIdAndStatus(userId, "PENDING")) {
            throw new IllegalStateException("You already have a pending application");
        }
        String emailNorm = workEmail.trim().toLowerCase();
        if (!domainAllowed(emailNorm)) throw new IllegalArgumentException("Work email domain not allowed");

        MedicApplication app = new MedicApplication();
        app.setUserId(userId);
        app.setLicenseNumber(licenseNo.trim());
        app.setLicenseIssuer(issuer.trim());
        app.setWorkEmail(emailNorm);
        app.setDocUrls(docUrls == null ? List.of() : docUrls);
        app.setStatus("PENDING");
        app.setSubmittedAt(Instant.now());
        app.setEmailVerified(Boolean.FALSE);

        String token = UUID.randomUUID().toString();
        app.setEmailToken(token);
        app = appRepo.save(app);

        // Send verify email to applicant
        email.sendWorkEmailVerification(emailNorm, token);

        // Notify admins (email + optional WS)
        String link = frontendBase + "/admin/medics";
        email.notifyAdminsNewApplication(
                "New medic application pending",
                """
                <p>A new medic application is pending review.</p>
                <ul>
                  <li>Applicant userId: %s</li>
                  <li>License: %s (%s)</li>
                  <li>Work email: %s</li>
                </ul>
                <p><a href="%s">Open in Admin</a></p>
                """.formatted(userId, app.getLicenseNumber(), app.getLicenseIssuer(), app.getWorkEmail(), link)
        );
        try {
            messaging.convertAndSend("/queue/admin/medic-apps",
                    new AdminPing("NEW_APPLICATION", app.getId(), app.getUserId(), Instant.now().toString()));
        } catch (Exception ignore) {}
        return app;
    }

    @Transactional
    @Override
    public MedicApplication verifyEmailToken(String token) {
        MedicApplication app = appRepo.findByEmailToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        app.setEmailVerified(Boolean.TRUE);
        app.setEmailToken(null);
        appRepo.save(app);

        // Notify admins that email is verified (optional)
        String link = frontendBase + "/admin/medics/" + app.getId();
        email.notifyAdminsNewApplication(
                "Medic application email verified",
                """
                <p>Applicant verified their work email.</p>
                <ul>
                  <li>Application id: %s</li>
                  <li>User id: %s</li>
                  <li>Work email: %s</li>
                </ul>
                <p><a href="%s">Review now</a></p>
                """.formatted(app.getId(), app.getUserId(), app.getWorkEmail(), link)
        );
        try {
            messaging.convertAndSend("/queue/admin/medic-apps",
                    new AdminPing("EMAIL_VERIFIED", app.getId(), app.getUserId(), Instant.now().toString()));
        } catch (Exception ignore) {}

        return app;
    }

    @Transactional
    @Override
    public MedicApplication approve(Long appId, Long adminUserId) {
        MedicApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        app.setStatus("APPROVED");
        app.setReviewedBy(adminUserId);
        app.setReviewedAt(Instant.now());
        appRepo.save(app);

        roleService.addRole(app.getUserId(), "ROLE_MEDIC");
        email.notifyApproved(app.getWorkEmail());

        try {
            messaging.convertAndSend("/queue/admin/medic-apps",
                    new AdminPing("APPROVED", app.getId(), app.getUserId(), Instant.now().toString()));
        } catch (Exception ignore) {}
        return app;
    }

    @Transactional
    @Override
    public MedicApplication reject(Long appId, Long adminUserId, String reason) {
        MedicApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        app.setStatus("REJECTED");
        app.setReviewedBy(adminUserId);
        app.setReviewedAt(Instant.now());
        app.setRejectReason(reason);
        appRepo.save(app);

        email.notifyRejected(app.getWorkEmail(), reason == null ? "" : reason);

        try {
            messaging.convertAndSend("/queue/admin/medic-apps",
                    new AdminPing("REJECTED", app.getId(), app.getUserId(), Instant.now().toString()));
        } catch (Exception ignore) {}
        return app;
    }

    public record AdminPing(String type, Long appId, Long userId, String at) {}
}
