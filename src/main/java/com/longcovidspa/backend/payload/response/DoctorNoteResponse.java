package com.longcovidspa.backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorNoteResponse {
    private Long id;
    private Instant timestamp;
    private String message;
    private String doctor; // doctor's email
} 