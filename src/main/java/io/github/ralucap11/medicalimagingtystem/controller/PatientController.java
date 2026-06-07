package io.github.ralucap11.medicalimagingtystem.controller;


import io.github.ralucap11.medicalimagingtystem.dto.DoctorResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/patient")
public class PatientController
{
    private final PatientService patientService;

    public PatientController(PatientService patientService)
    {
        this.patientService = patientService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(patientService.getPatientById(id));
        }
        catch (ResourceNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients()
    {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN, PATIENT')")
    public ResponseEntity<PatientResponseDTO> createPatient(@RequestBody PatientRequestDTO request)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(request));
        }
         catch (ResourceAlreadyExists e)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id, @RequestBody PatientRequestDTO request)
    {
        try
        {
            return ResponseEntity.ok(patientService.updatePatient(id,request));
        }
        catch (ResourceNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (ResourceAlreadyExists e)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id)
    {
        try
        {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        }
        catch (ResourceNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponseDTO> getMyInfo(Authentication authentication) {
        try {
            String email = authentication.getName();
            PatientResponseDTO patient = patientService.findByEmail(email);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{patientId}/doctors")
    public ResponseEntity<List<DoctorResponseDTO>> getDoctorsForPatient(@PathVariable Long patientId)
    {
        return ResponseEntity.ok(patientService.getDoctorsForPatient(patientId));
    }
}
