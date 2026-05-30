package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.XrayResponseDTO;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.service.XrayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/xray")
@EnableMethodSecurity
public class XrayController
{
    private final XrayService xrayService;

    public XrayController(XrayService xrayService)
    {
        this.xrayService = xrayService;
    }

    @GetMapping
    public ResponseEntity<List<XrayResponseDTO>> getAllXrays()
    {
        return ResponseEntity.ok(xrayService.getAllXrays());
    }


    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<XrayResponseDTO>> getPatientXrays(@PathVariable Long patientId)
    {
        try
        {
            return ResponseEntity.ok(xrayService.getPatientXrays(patientId));
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

    @GetMapping("/{id}")
    public ResponseEntity<XrayResponseDTO> getXrayById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(xrayService.getXrayById(id));
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

    @PostMapping(value = "/upload/{patientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<XrayResponseDTO> uploadXray(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String xrayName,
            @RequestParam(required = false) String description
    )
    {
        try
        {
            return ResponseEntity.ok(xrayService.uploadXray(patientId, file, xrayName, description));
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> deleteXray(@PathVariable Long id)
    {
        try
        {
            xrayService.deleteXray(id);
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
