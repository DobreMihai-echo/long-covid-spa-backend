package com.longcovidspa.backend.services;

import com.longcovidspa.backend.model.MedicApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicVerificationService {
    Page<MedicApplication> list(String status, Pageable pageable);

    MedicApplication get(Long id);

    MedicApplication submit(Long userId, String licenseNo, String issuer, String workEmail, List<String> docUrls);

    MedicApplication verifyEmailToken(String token);

    MedicApplication approve(Long appId, Long adminUserId);

    MedicApplication reject(Long appId, Long adminUserId, String reason);
}

