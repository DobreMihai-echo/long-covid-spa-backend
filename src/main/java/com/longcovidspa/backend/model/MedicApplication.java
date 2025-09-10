package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="medic_application")
@Getter @Setter @NoArgsConstructor
public class MedicApplication {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long userId;
    private Long providerDirectoryId;
    private String licenseNumber;
    private String licenseIssuer;
    private String workEmail;
    private String status;       // PENDING / APPROVED / REJECTED
    private Instant submittedAt = Instant.now();
    private Long reviewedBy;
    private Instant reviewedAt;
    @Column(length=2000) private String rejectReason;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition="jsonb")
    private List<String> docUrls = new ArrayList<>();
    private Boolean emailVerified = false;
    private String emailToken;
}
