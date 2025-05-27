package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {
    List<Symptom> findByUserId(Long userId);
}