package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.DoctorNote;
import com.longcovidspa.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByPatientIdOrderByTimestampDesc(Long patientId);
    List<DoctorNote> findByDoctorAndPatientOrderByTimestampDesc(User doctor, User patient);
} 