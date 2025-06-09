package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.ERole;
import com.longcovidspa.backend.model.Role;
import com.longcovidspa.backend.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        for (ERole role : ERole.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(role));
                System.out.println("Inserted role: " + role);
            }
        }
    }
}
