package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.PatientResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.RegisterRequestDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import io.github.ralucap11.medicalimagingtystem.entity.Role;
import io.github.ralucap11.medicalimagingtystem.entity.User;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.repository.PatientRepository;
import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService
{
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PatientRepository patientRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PatientResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExists("user already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PATIENT);

        Patient patient = new Patient();

        patient.setUser(user);
        user.setPatient(patient);

        Patient savedPatient = patientRepository.save(patient);

        return entityToDTO(patient);
    }

    private PatientResponseDTO entityToDTO(Patient patient)
    {
        PatientResponseDTO response = new PatientResponseDTO();

        User user = patient.getUser();

        response.setId(patient.getId());
        response.setLastName(user.getLastName());
        response.setFirstName(user.getFirstName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        response.setCnp(patient.getCnp());
        response.setWeight(patient.getWeight());
        response.setAge(patient.getAge());
        response.setGender(patient.getGender());
        response.setHeight((patient.getHeight()));

        return response;
    }
}
