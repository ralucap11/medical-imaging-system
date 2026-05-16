package io.github.ralucap11.medicalimagingtystem.dto;

import lombok.Data;

@Data
public class XrayResponseDTO
{
    private Long id;
    private String format;
    private String fileName;

    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
}
