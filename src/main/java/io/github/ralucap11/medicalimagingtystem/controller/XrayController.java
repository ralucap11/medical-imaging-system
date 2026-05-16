package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.XrayResponseDTO;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.service.XrayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/xray")
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<XrayResponseDTO> uploadXray(
            @RequestParam Long patientId,
            @RequestParam("file") MultipartFile file)
    {
        try
        {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(xrayService.uploadXray(patientId, file));
        }
        catch (ResourceNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
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
