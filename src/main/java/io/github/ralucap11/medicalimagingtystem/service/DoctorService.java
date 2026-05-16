package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.DoctorRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DoctorResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Doctor;
import io.github.ralucap11.medicalimagingtystem.entity.User;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.DoctorRepository;import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService
{
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public DoctorService(UserRepository userRepository, DoctorRepository doctorRepository)
    {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
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
        user.setPassword(request.getPassword());
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
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        Doctor updatedDoctor = doctorRepository.save(doctor);

        return entityToDTO(updatedDoctor);
    }

    public void deleteDoctor(Long id)
    {
        if(!doctorRepository.existsById(id))
        {
            throw new ResourceNotFoundException("doctor not found");
        }
        doctorRepository.deleteById(id);
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

        return response;
    }
}
