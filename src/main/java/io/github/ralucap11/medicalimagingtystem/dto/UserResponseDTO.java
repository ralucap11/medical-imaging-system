package io.github.ralucap11.medicalimagingtystem.dto;

import io.github.ralucap11.medicalimagingtystem.entity.Role;
import jakarta.persistence.*;
import lombok.Data;


@Data
public class UserResponseDTO
{
    private Long id;
    private String lastName;
    private String firstName;
    private String email;
    private Role role;

}

