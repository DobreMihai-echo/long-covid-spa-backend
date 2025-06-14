package com.longcovidspa.backend.services;

import com.longcovidspa.backend.exception.DoctorPatientAccessException;
import com.longcovidspa.backend.model.DoctorNote;
import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.payload.request.DoctorNoteRequest;
import com.longcovidspa.backend.payload.response.DoctorNoteResponse;
import com.longcovidspa.backend.payload.response.PatientDTO;
import com.longcovidspa.backend.repositories.DoctorNoteRepository;
import com.longcovidspa.backend.repositories.UserRepositories;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {
    
    private final UserRepositories userRepository;
    private final DoctorNoteRepository doctorNoteRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<PatientDTO> getAssignedPatients(String doctorUsername) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Use a direct query to fetch patients from the junction table
        String sql = "SELECT p.* FROM authentication p " +
                     "JOIN doctor_patients dp ON p.id = dp.patient_id " +
                     "WHERE dp.doctor_id = :doctorId";
        
        Query query = entityManager.createNativeQuery(sql, User.class);
        query.setParameter("doctorId", doctor.getId());
        
        @SuppressWarnings("unchecked")
        List<User> patients = query.getResultList();
        
        return patients.stream()
                .map(patient -> PatientDTO.builder()
                        .id(patient.getId())
                        .fullName(patient.getFirstName() + " " + patient.getLastName())
                        .email(patient.getEmail())
                        .lastSeen(patient.getHealthData() != null && !patient.getHealthData().isEmpty() ? 
                                patient.getHealthData().get(patient.getHealthData().size() - 1)
                                        .getReceivedDate().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignPatientToDoctor(Long doctorId, Long patientId) {
        // Check if relationship already exists
        String checkSql = "SELECT COUNT(*) FROM doctor_patients WHERE doctor_id = :doctorId AND patient_id = :patientId";
        Query checkQuery = entityManager.createNativeQuery(checkSql);
        checkQuery.setParameter("doctorId", doctorId);
        checkQuery.setParameter("patientId", patientId);
        
        Long count = ((Number) checkQuery.getSingleResult()).longValue();
        
        if (count == 0) {
            // Only insert if relationship doesn't exist
            String insertSql = "INSERT INTO doctor_patients (doctor_id, patient_id) VALUES (:doctorId, :patientId)";
            Query insertQuery = entityManager.createNativeQuery(insertSql);
            insertQuery.setParameter("doctorId", doctorId);
            insertQuery.setParameter("patientId", patientId);
            insertQuery.executeUpdate();
        }
    }
    
    @Transactional
    public void assignPatientToDoctorByUsername(String doctorUsername, Long patientId) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        assignPatientToDoctor(doctor.getId(), patientId);
    }
    
    @Transactional(readOnly = true)
    public boolean isDoctorAssignedToPatient(Long doctorId, Long patientId) {
        String sql = "SELECT COUNT(*) FROM doctor_patients WHERE doctor_id = :doctorId AND patient_id = :patientId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("doctorId", doctorId);
        query.setParameter("patientId", patientId);
        
        Long count = ((Number) query.getSingleResult()).longValue();
        return count > 0;
    }
    
    @Transactional
    public DoctorNoteResponse addNoteToPatient(String doctorUsername, Long patientId, DoctorNoteRequest noteRequest) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
                
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Verify the doctor is assigned to this patient
        if (!isDoctorAssignedToPatient(doctor.getId(), patientId)) {
            throw new DoctorPatientAccessException("Doctor is not assigned to this patient");
        }
        
        // Create and save the note
        DoctorNote note = DoctorNote.builder()
                .doctor(doctor)
                .patient(patient)
                .message(noteRequest.getMessage())
                .timestamp(Instant.now())
                .build();
                
        doctorNoteRepository.save(note);
        
        // Return response
        return DoctorNoteResponse.builder()
                .id(note.getId())
                .timestamp(note.getTimestamp())
                .message(note.getMessage())
                .doctor(doctor.getEmail())
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<DoctorNoteResponse> getPatientNotes(String doctorUsername, Long patientId) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
                
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Verify the doctor is assigned to this patient
        if (!isDoctorAssignedToPatient(doctor.getId(), patientId)) {
            throw new DoctorPatientAccessException("Doctor is not assigned to this patient");
        }
        
        List<DoctorNote> notes = doctorNoteRepository.findByDoctorAndPatientOrderByTimestampDesc(doctor, patient);
        
        return notes.stream()
                .map(note -> DoctorNoteResponse.builder()
                        .id(note.getId())
                        .timestamp(note.getTimestamp())
                        .message(note.getMessage())
                        .doctor(note.getDoctor().getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}
