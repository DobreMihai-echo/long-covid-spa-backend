package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="privacy_export_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJob {
    public enum Status { PENDING, READY, FAILED }

    @Id
    @Column(length=36)
    private String id; // UUID string

    @Column(nullable=false) private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Status status;

    @Column(nullable=false) private Instant createdAt;
    private Instant readyAt;
    private Instant expiresAt;

    private String filePath;    // temp storage path
    private String downloadToken; // random; query param

    private String errorMsg;
}
