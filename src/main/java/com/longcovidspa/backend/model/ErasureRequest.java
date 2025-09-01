package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="privacy_erasure_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErasureRequest {
    public enum Status { REQUESTED, IN_PROGRESS, DONE, FAILED }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Status status;

    @Column(nullable=false) private Instant requestedAt;
    private Instant executedAt;
    private String errorMsg;
}
