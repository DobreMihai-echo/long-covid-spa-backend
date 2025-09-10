package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.utils.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ConsoleEmailService implements EmailService {
    private final JavaMailSender sender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.admin.notify.emails:}")
    private String adminEmailsCsv;

    @Value("${app.frontend.base-url}")
    private String frontendBase;

    public ConsoleEmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    private void sendHtmlToAdmins(String subject, String html) {
        if (adminEmailsCsv == null || adminEmailsCsv.isBlank()) return;
        for (String to : adminEmailsCsv.split(",")) {
            String email = to.trim();
            if (email.isEmpty()) continue;
            sendHtml(email, subject, html);
        }
    }

    @Override
    public void sendWorkEmailVerification(String to, String token) {
        String verifyUrl = frontendBase + "/medic/verify-email?token=" + token;
        String subject = "Verify your work email";
        String html = """
      <div style="font-family:Inter,Arial,sans-serif;color:#111;">
        <h2>Verify your work email</h2>
        <p>Click the button below to verify your work email address.</p>
        <p style="margin:22px 0">
          <a href="%s" style="background:#2563eb;color:#fff;padding:10px 16px;border-radius:8px;text-decoration:none;">
            Verify Email
          </a>
        </p>
        <p>If the button doesn't work, paste this link into your browser:<br>%s</p>
      </div>
    """.formatted(verifyUrl, verifyUrl);

        sendHtml(to, subject, html);
    }

    @Override
    public void notifyApproved(String to) {
        sendHtml(to, "Medic application approved",
                "<p>Your medic application has been <b>approved</b>. You can now sign in and access medic features.</p>");
    }

    @Override
    public void notifyRejected(String to, String reason) {
        sendHtml(to, "Medic application rejected",
                "<p>Your medic application was <b>rejected</b>.</p><p>Reason: "+escape(reason)+"</p>");
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, "UTF-8");
            h.setFrom(fromEmail, "CovidYouNot");   // header From == your@yahoo.com
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            sender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void notifyAdminsNewApplication(String subject, String htmlBody) {
        sendHtmlToAdmins(subject, htmlBody);
    }

    private static String escape(String s){ return s==null?"":s.replace("<","&lt;").replace(">","&gt;"); }
}