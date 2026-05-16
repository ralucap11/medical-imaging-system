package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.DoctorRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DoctorResponseDTO;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

}
