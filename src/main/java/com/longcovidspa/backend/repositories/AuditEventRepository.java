package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> { }
