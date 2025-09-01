package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name="privacy_consent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long userId;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name="privacy_consent_scopes", joinColumns=@JoinColumn(name="consent_id"))
    @Column(name="scope")
    private Set<ConsentScope> scopes;

    @Column(nullable=false) private String policyVersion; // e.g. "2025-09-01"
    @Column(nullable=false) private String textHash;      // sha256 of policy text used
    @Column(nullable=false) private Instant grantedAt;
    private Instant withdrawnAt;
}
