package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ExportJobRepository extends JpaRepository<ExportJob, String> {
    List<ExportJob> findByExpiresAtBefore(Instant ts);
}
