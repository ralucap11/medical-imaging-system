package io.github.ralucap11.medicalimagingtystem.dto;

import lombok.Data;

@Data

public class PatientSummaryDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String gender;

    private String email;
    private String cnp;
}
