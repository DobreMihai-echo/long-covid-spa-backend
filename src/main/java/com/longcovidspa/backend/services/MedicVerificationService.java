package com.longcovidspa.backend.services;

import com.longcovidspa.backend.model.MedicApplication;

public interface MedicVerificationService {
    MedicApplication submit(Long userId, String licenseNo, String issuer, String workEmail, List<String> docUrls);
    MedicApplication verifyEmailToken(String token);
    MedicApplication approve(Long appId, Long adminUserId);
    MedicApplication reject(Long appId, Long adminUserId, String reason);

