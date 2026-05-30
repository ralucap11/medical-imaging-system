package io.github.ralucap11.medicalimagingtystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiagnosisRequestDTO {
    private String title;
    private String description;
    private LocalDate date;
}
