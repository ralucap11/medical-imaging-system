package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.DoctorRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DoctorResponseDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientSummaryDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Doctor;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import io.github.ralucap11.medicalimagingtystem.entity.User;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.DoctorRepository;
import io.github.ralucap11.medicalimagingtystem.repository.PatientRepository;
import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService
{
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;

    public DoctorService(UserRepository userRepository, DoctorRepository doctorRepository,
                         PatientRepository patientRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public DoctorResponseDTO getDoctorById(Long id)
  {
      if(!doctorRepository.existsById(id))
      {
          throw new ResourceNotFoundException("doctor not found");
      }

      Doctor doctor = doctorRepository.getReferenceById(id);
      return entityToDTO(doctor);

  }

    public List<DoctorResponseDTO> getAllDoctors()
    {
        return doctorRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .toList();
    }

    public DoctorResponseDTO createDoctor(DoctorRequestDTO request)
    {
        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new ResourceAlreadyExists("doctor already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());

        Doctor doctor = new Doctor();
        doctor.setSpecialty(request.getSpecialty());

        doctor.setUser(user);
        user.setDoctor(doctor);

        Doctor createdDoctor = doctorRepository.save(doctor);

        return  entityToDTO(createdDoctor);
    }

    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO request)
    {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        doctor.setSpecialty(request.getSpecialty());

        User user = doctor.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return entityToDTO(updatedDoctor);
    }

    public void deleteDoctor(Long id)
    {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        doctor.getPatients().clear();

        doctorRepository.delete(doctor);
    }

    public DoctorResponseDTO getByEmail(String email) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));
        return entityToDTO(doctor);
    }


    private DoctorResponseDTO entityToDTO(Doctor doctor)
    {
        DoctorResponseDTO response = new DoctorResponseDTO();

        User user = doctor.getUser();

        response.setId(doctor.getId());
        response.setLastName(user.getLastName());
        response.setFirstName(user.getFirstName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        response.setSpecialty(doctor.getSpecialty());
        response.setPatients(doctor.getPatients().stream()
                .map(this::patientToSummaryDTO)
                .toList()
        );

        return response;
    }

    public DoctorResponseDTO assignPatient(Long doctorId, Long patientId)
    {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        if (doctor.getPatients().contains(patient))
        {
            throw new ResourceAlreadyExists("patient already assigned to this doctor");
        }

        doctor.getPatients().add(patient);
        patient.getDoctors().add(doctor);

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return entityToDTO(updatedDoctor);
    }

    public DoctorResponseDTO unassignPatient(Long doctorId, Long patientId)
    {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        doctor.getPatients().remove(patient);
        patient.getDoctors().remove(doctor);

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return entityToDTO(updatedDoctor);
    }


    public List<PatientSummaryDTO> getPatientsForDoctor(Long doctorId)
    {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        return doctor.getPatients()
                .stream()
                .map(this::patientToSummaryDTO)
                .toList();
    }

    private PatientSummaryDTO patientToSummaryDTO(Patient patient)
    {
        PatientSummaryDTO dto = new PatientSummaryDTO();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getUser().getFirstName());
        dto.setLastName(patient.getUser().getLastName());
        dto.setEmail(patient.getUser().getEmail());
        dto.setCnp(patient.getCnp());
        dto.setAge(patient.getAge());
        dto.setGender(String.valueOf(patient.getGender()));
        return dto;
    }
}
