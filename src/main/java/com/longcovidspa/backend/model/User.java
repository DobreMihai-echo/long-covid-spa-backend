package com.longcovidspa.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "authentication")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Include
    @Column(name = "email")
    private String email;

    @ToString.Include
    private String username;

    private String firstName;
    private String lastName;
    private String password;
    private String gender;
    private Date dateOfBirth;
    private Integer height;
    private Integer weight;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HealthData> healthData;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_patients",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private Set<User> assignedPatients = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "assignedPatients", fetch = FetchType.LAZY)
    private Set<User> doctors = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String email, String firstName, String lastName,
                String gender, Date dateOfBirth, Integer height, Integer weight, String password) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.height = height;
        this.weight = weight;
        this.password = password;
    }
}


