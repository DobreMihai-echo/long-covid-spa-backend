package com.longcovidspa.backend.services.impl;
import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.model.UserLite;
import com.longcovidspa.backend.repositories.UserRepositories;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class PatientsAssignmentServiceImpl {
    private final UserRepositories userRepo;

    private static UserLite toLite(User u) {
        return new UserLite(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName());
    }

    public Page<UserLite> listPatientsOfDoctor(Long doctorId, Pageable pageable) {
        if (!userRepo.isMedic(doctorId)) throw new IllegalArgumentException("User is not a medic");
        return userRepo.findPatientsOfDoctor(doctorId, pageable).map(PatientsAssignmentServiceImpl::toLite);
    }

    public Page<UserLite> searchAssignablePatients(String q, Pageable pageable) {
        return userRepo.searchPatients(q, pageable).map(PatientsAssignmentServiceImpl::toLite);
    }

    @Transactional
    public void assignPatient(Long doctorId, Long patientId, Long adminUserId) {
        if (!userRepo.isMedic(doctorId)) throw new IllegalArgumentException("User is not a medic");

        User doctor = userRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        User patient = userRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (doctorId.equals(patientId)) throw new IllegalArgumentException("Cannot assign doctor to self");

        // Add both sides for in-memory consistency; owning side is doctor.assignedPatients
        boolean added = doctor.getAssignedPatients().add(patient);
        if (!added) return; // already assigned; idempotent

        patient.getDoctors().add(doctor);
        userRepo.save(doctor);
    }

    @Transactional
    public void unassignPatient(Long doctorId, Long patientId, Long adminUserId) {
        if (!userRepo.isMedic(doctorId)) throw new IllegalArgumentException("User is not a medic");

        User doctor = userRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        User patient = userRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        doctor.getAssignedPatients().remove(patient);
        patient.getDoctors().remove(doctor);
        userRepo.save(doctor);
    }
}
