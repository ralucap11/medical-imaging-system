package io.github.ralucap11.medicalimagingtystem.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO
{
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
