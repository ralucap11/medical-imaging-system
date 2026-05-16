package io.github.ralucap11.medicalimagingtystem.dto;
;
import lombok.Data;

@Data
public class XrayRequestDTO
{
    private String format;
    private String fileName;
    private Long patientId;
}
