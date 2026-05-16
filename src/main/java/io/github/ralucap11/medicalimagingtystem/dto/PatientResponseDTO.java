package io.github.ralucap11.medicalimagingtystem.dto;

import io.github.ralucap11.medicalimagingtystem.entity.Gender;
import io.github.ralucap11.medicalimagingtystem.entity.Role;
import lombok.Data;

@Data
public class PatientResponseDTO
{
    private Long id;
    private String lastName;
    private String firstName;
    private String email;
    private String password;
    private Role role;

    private String cnp;
    private Double weight;
    private Integer age;
    private Gender gender;
    private Double height;
}
