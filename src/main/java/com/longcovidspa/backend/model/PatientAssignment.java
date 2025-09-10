package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="patient_assignment")
@Getter @Setter @NoArgsConstructor
public class PatientAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long medicId;
    private Long patientId;
    private Instant grantedAt = Instant.now();
    private Instant revokedAt;  // null => active
}
