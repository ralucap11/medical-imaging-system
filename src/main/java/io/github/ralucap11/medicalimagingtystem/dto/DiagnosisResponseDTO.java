package io.github.ralucap11.medicalimagingtystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiagnosisResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private Long xrayId;
    private String xrayName;
    private String doctorFirstName;
    private String doctorLastName;

    private String aiClassification; 
    private Double cobbAngle;
}
