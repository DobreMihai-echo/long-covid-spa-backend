package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="privacy_audit_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long userId;
    @Column(nullable=false) private String action;   // e.g. EXPORT_REQUESTED, EXPORT_DOWNLOADED, CONSENT_GIVEN
    @Column(nullable=false) private String resource; // e.g. "export:jobId", "consent:policyVersion"
    @Column(nullable=false) private Instant createdAt;
    private String ip;
    private String agent;
}
