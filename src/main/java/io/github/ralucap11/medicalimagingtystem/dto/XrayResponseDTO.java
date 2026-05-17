package io.github.ralucap11.medicalimagingtystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class XrayResponseDTO
{
    private Long id;
    private String format;
    private String fileName;
    private String xrayName;
    private String description;
    private LocalDate dateUploaded;


    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
}
