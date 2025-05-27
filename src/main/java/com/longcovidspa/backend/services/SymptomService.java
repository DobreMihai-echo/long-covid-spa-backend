package com.longcovidspa.backend.services;

import com.longcovidspa.backend.model.Symptom;
import com.longcovidspa.backend.repositories.SymptomRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SymptomService {

    private final SymptomRepository symptomRepository;

    public SymptomService(SymptomRepository symptomRepository) {
        this.symptomRepository = symptomRepository;
    }

    public Symptom logSymptom(Symptom symptom) {
        return symptomRepository.save(symptom);
    }

    public List<Symptom> getSymptomsByUserId(Long userId) {
        return symptomRepository.findByUserId(userId);
    }
}