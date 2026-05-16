package io.github.ralucap11.medicalimagingtystem.dto;

import io.github.ralucap11.medicalimagingtystem.entity.Role;
import lombok.Data;

@Data
public class UserRequestDTO
{
    private String lastName;
    private String firstName;
    private String email;
    private String password;
    private Role role;


}
