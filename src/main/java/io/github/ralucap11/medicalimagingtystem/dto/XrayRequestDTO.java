package io.github.ralucap11.medicalimagingtystem.dto;
;
import lombok.Data;

import java.time.LocalDate;

@Data
public class XrayRequestDTO
{
    private String format;
    private String fileName;
    private String xrayName;
    private String description;
    private LocalDate dateUploaded;

    private Long patientId;
}
