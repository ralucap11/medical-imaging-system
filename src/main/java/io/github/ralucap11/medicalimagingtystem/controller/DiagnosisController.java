package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisResponseDTO;
import io.github.ralucap11.medicalimagingtystem.service.DiagnosisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PutMapping("/patient/{patientId}")
    public ResponseEntity<DiagnosisResponseDTO> saveDiagnosis(
            @PathVariable Long patientId,
            @RequestBody DiagnosisRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(
                diagnosisService.saveDiagnosis(patientId, request, authentication.getName()));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<DiagnosisResponseDTO> getDiagnosis(@PathVariable Long patientId) {
        return ResponseEntity.ok(diagnosisService.getDiagnosisByPatient(patientId));
    }
}