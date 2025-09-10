package com.longcovidspa.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="provider_directory")
@Getter @Setter @NoArgsConstructor
public class ProviderDirectory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String fullName;
    private String licenseNumber;
    private String licenseIssuer;
    private String workEmailDomain; // e.g. hospital.ro
    private Boolean active = true;
}