package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.DoctorRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DoctorResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientSummaryDTO;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")

public class DoctorController
{
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService)
    {
        this.doctorService = doctorService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(doctorService.getDoctorById(id));
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
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors()
    {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(@RequestBody DoctorRequestDTO request)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(request));
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
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable Long id, @RequestBody DoctorRequestDTO request)
    {
        try
        {
            return ResponseEntity.ok(doctorService.updateDoctor(id,request));
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id)
    {
        try
        {
            doctorService.deleteDoctor(id);
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
    public ResponseEntity<DoctorResponseDTO> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getByEmail(userDetails.getUsername()));
    }

    @PostMapping("/{doctorId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<DoctorResponseDTO> assignPatient(
            @PathVariable Long doctorId,
            @PathVariable Long patientId)
    {
        try
        {
            return ResponseEntity.ok(doctorService.assignPatient(doctorId, patientId));
        }
        catch (ResourceNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (ResourceAlreadyExists e)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        }
    }

    @DeleteMapping("/{doctorId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<DoctorResponseDTO> unassignPatient(
            @PathVariable Long doctorId,
            @PathVariable Long patientId)
    {
        try
        {
            return ResponseEntity.ok(doctorService.unassignPatient(doctorId, patientId));
        }
        catch (ResourceNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    // Listează pacienții unui doctor
    @GetMapping("/{doctorId}/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<PatientSummaryDTO>> getPatients(
            @PathVariable Long doctorId)
    {
        return ResponseEntity.ok(doctorService.getPatientsForDoctor(doctorId));
    }

}
