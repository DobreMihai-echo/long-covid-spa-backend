package com.longcovidspa.backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ValidationErrorResponse {
    private int status = 400;
    private String message = "Validation failed";
    private long timestamp = System.currentTimeMillis();
    private List<FieldErrorDetails> errors = new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldErrorDetails {
        private String field;
        private Object rejectedValue;
        private String message;
    }
} 