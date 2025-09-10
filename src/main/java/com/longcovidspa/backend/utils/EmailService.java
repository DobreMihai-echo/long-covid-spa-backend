package com.longcovidspa.backend.utils;

public interface EmailService {
    void sendWorkEmailVerification(String to, String token);
    void notifyApproved(String to);
    void notifyRejected(String to, String reason);
    void notifyAdminsNewApplication(String subject, String htmlBody);
}
