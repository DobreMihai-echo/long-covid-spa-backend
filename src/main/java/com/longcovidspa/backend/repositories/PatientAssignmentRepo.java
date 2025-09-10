package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.PatientAssignment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientAssignmentRepo extends JpaRepository<PatientAssignment, Long> {

    @Query("select (count(pa) > 0) from PatientAssignment pa " +
            "where pa.medicId = :medic and pa.patientId = :patient and pa.revokedAt is null")
    boolean existsActive(@Param("medic") Long medicId, @Param("patient") Long patientId);
}
