package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.model.ERole;
import com.longcovidspa.backend.model.Role;
import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.repositories.RoleRepository;
import com.longcovidspa.backend.repositories.UserRepositories;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final UserRepositories users;
    private final RoleRepository roles;

    @Transactional
    public void addRole(Long userId, String roleName) {
        User u = users.findById(userId).orElseThrow();
        Role r = roles.findByName(ERole.valueOf(roleName)).orElseThrow();
        if (u.getRoles().stream().noneMatch(x -> x.getName().equals(roleName))) {
            u.getRoles().add(r);
            users.save(u);
        }
    }
}
