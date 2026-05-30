package io.github.ralucap11.medicalimagingtystem.controller;


import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisResponseDTO;
import io.github.ralucap11.medicalimagingtystem.service.DiagnosisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController
{
    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/xray/{xrayId}")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public ResponseEntity<DiagnosisResponseDTO> addDiagnosis(@PathVariable Long xrayId, @RequestBody DiagnosisRequestDTO request,  Authentication authentication)
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(diagnosisService.addDiagnosis(xrayId, request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public ResponseEntity<DiagnosisResponseDTO> updateDiagnosis(
            @PathVariable Long id,
            @RequestBody DiagnosisRequestDTO request) {
        return ResponseEntity.ok(diagnosisService.updateDiagnosis(id, request));
    }

    @GetMapping("/xray/{xrayId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<DiagnosisResponseDTO> getDiagnosisByXray(
            @PathVariable Long xrayId) {
        return ResponseEntity.ok(diagnosisService.getDiagnosisByXray(xrayId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<DiagnosisResponseDTO>> getDiagnosesByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(diagnosisService.getDiagnosesByPatient(patientId));
    }

}
