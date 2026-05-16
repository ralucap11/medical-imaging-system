package io.github.ralucap11.medicalimagingtystem.controller;


import io.github.ralucap11.medicalimagingtystem.dto.PatientRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientResponseDTO;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

}
