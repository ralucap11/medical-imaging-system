package io.github.ralucap11.medicalimagingtystem.dto;

import io.github.ralucap11.medicalimagingtystem.entity.Role;
import lombok.Data;

import java.util.List;

@Data
public class DoctorResponseDTO
{
    private Long id;
    private String lastName;
    private String firstName;
    private String email;
    private String password;
    private Role role;

    private String specialty;

    private List<PatientSummaryDTO> patients;
}
