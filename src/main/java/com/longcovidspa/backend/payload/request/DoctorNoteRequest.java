package com.longcovidspa.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorNoteRequest {
    
    @NotBlank(message = "Note message cannot be empty")
    @Size(max = 1000, message = "Note message cannot exceed 1000 characters")
    private String message;
} 