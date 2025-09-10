package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.model.UserLite;
import com.longcovidspa.backend.payload.request.DoctorNoteRequest;
import com.longcovidspa.backend.payload.response.DoctorNoteResponse;
import com.longcovidspa.backend.payload.response.PatientDTO;
import com.longcovidspa.backend.payload.response.UserInfoDTO;
import com.longcovidspa.backend.repositories.UserRepositories;
import com.longcovidspa.backend.security.UserDetailsImpl;
import com.longcovidspa.backend.services.DoctorService;
import com.longcovidspa.backend.dto.PatientDashboardDTO;
import com.longcovidspa.backend.services.impl.PatientsAssignmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final UserRepositories userRepository;

    private final PatientsAssignmentServiceImpl patientService;

    @GetMapping("/me/patients")
    @PreAuthorize("hasAnyRole('ADMIN','MEDIC')")
    public ResponseEntity<Page<UserLite>> myPatients(
            @AuthenticationPrincipal UserDetailsImpl me,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(patientService.listPatientsOfDoctor(me.getId(), PageRequest.of(page, size)));
    }


    @PostMapping("/assign")
    @PreAuthorize("hasRole('ROLE_MEDIC')")
    public ResponseEntity<?> assignPatient(@RequestParam Long doctorId, @RequestParam Long patientId) {
        doctorService.assignPatientToDoctor(doctorId, patientId);
        return ResponseEntity.ok("Patient assigned successfully");
    }
    
    @PostMapping("/assign-to-me")
    @PreAuthorize("hasRole('ROLE_MEDIC')")
    public ResponseEntity<?> assignPatientToCurrentDoctor(Authentication authentication, @RequestParam Long patientId) {
        String doctorUsername = authentication.getName();
        doctorService.assignPatientToDoctorByUsername(doctorUsername, patientId);
        return ResponseEntity.ok("Patient assigned successfully to current doctor");
    }
    
    @GetMapping("/current-user")
    @PreAuthorize("hasRole('ROLE_MEDIC')")
    public ResponseEntity<?> getCurrentUserInfo(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Count patients directly from the database
            List<PatientDTO> patients = doctorService.getAssignedPatients(username);
            
            UserInfoDTO userInfo = UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()))
                .assignedPatientsCount(patients.size())
                .build();
            return ResponseEntity.ok(userInfo);
        }
        
        return ResponseEntity.badRequest().body("User not found");
    }
    
    @PostMapping("/patient/{patientId}/note")
    @PreAuthorize("hasRole('ROLE_MEDIC')")
    public ResponseEntity<DoctorNoteResponse> addNoteToPatient(
            Authentication authentication, 
            @PathVariable Long patientId,
            @Valid @RequestBody DoctorNoteRequest noteRequest) {
        
        String doctorUsername = authentication.getName();
        DoctorNoteResponse response = doctorService.addNoteToPatient(doctorUsername, patientId, noteRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/patient/{patientId}/notes")
    @PreAuthorize("hasRole('ROLE_MEDIC')")
    public ResponseEntity<List<DoctorNoteResponse>> getPatientNotes(
            Authentication authentication,
            @PathVariable Long patientId) {
        
        String doctorUsername = authentication.getName();
        List<DoctorNoteResponse> notes = doctorService.getPatientNotes(doctorUsername, patientId);
        return ResponseEntity.ok(notes);
    }
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_MEDIC')")
    public ResponseEntity<List<PatientDashboardDTO>> getDashboardPatients(Authentication authentication) {
        String doctorUsername = authentication.getName();
        List<PatientDashboardDTO> patients = doctorService.getPatientsForDashboard(doctorUsername);
        return ResponseEntity.ok(patients);
    }
}
