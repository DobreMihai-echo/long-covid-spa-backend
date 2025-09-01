package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.model.AuditEvent;
import com.longcovidspa.backend.repositories.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditEventRepository repo;
    public void log(Long userId, String action, String resource, String ip, String agent) {
        repo.save(AuditEvent.builder()
                .userId(userId).action(action).resource(resource)
                .createdAt(Instant.now()).ip(ip).agent(agent).build());
    }
}