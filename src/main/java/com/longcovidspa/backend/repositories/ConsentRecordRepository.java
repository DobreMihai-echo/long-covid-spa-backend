package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {
    List<ConsentRecord> findByUserIdOrderByGrantedAtDesc(Long userId);
}
