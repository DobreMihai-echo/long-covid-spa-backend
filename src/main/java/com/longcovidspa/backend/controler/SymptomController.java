package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.model.Symptom;
import com.longcovidspa.backend.services.SymptomService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
public class SymptomController {

    private final SymptomService symptomService;

    public SymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    // POST /api/symptoms → Logs a symptom into the database.
    @PostMapping
    public ResponseEntity<?> logSymptom(@RequestBody Symptom symptom) {
        Symptom savedSymptom = symptomService.logSymptom(symptom);
        return ResponseEntity.ok(savedSymptom);
    }

    // GET /api/symptoms/{userId} → Retrieves past symptoms for trend analysis.
    @GetMapping("/{userId}")
    public ResponseEntity<?> getSymptomsByUser(@PathVariable Long userId) {
        List<Symptom> symptoms = symptomService.getSymptomsByUserId(userId);
        return ResponseEntity.ok(symptoms);
    }
}