package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.PatientRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.PatientResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import io.github.ralucap11.medicalimagingtystem.entity.User;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.PatientRepository;
import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService
{
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    public PatientService(UserRepository userRepository, PatientRepository patientRepository)
    {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

   public PatientResponseDTO getPatientById(Long id)
   {
       if(!patientRepository.existsById(id))
       {
           throw new ResourceNotFoundException("patient not found");
       }

       Patient patient = patientRepository.getReferenceById(id);

       return entityToDTO(patient);
   }

   public List<PatientResponseDTO> getAllPatients()
   {
       return patientRepository.findAll()
               .stream()
               .map(this::entityToDTO)
               .toList();
   }

   public PatientResponseDTO createPatient(PatientRequestDTO request)
   {
       if (userRepository.existsByEmail(request.getEmail()))
       {
           throw new ResourceAlreadyExists("email already exists");
       }
       if(patientRepository.existsByCnp(request.getCnp()))
       {
           throw new ResourceAlreadyExists("patient with this CNP already exists");
       }

       User user = new User();
       user.setEmail(request.getEmail());
       user.setPassword(request.getPassword());
       user.setFirstName(request.getFirstName());
       user.setLastName(request.getLastName());
       user.setRole(request.getRole());

       Patient patient = new Patient();
       patient.setCnp(request.getCnp());
       patient.setWeight(request.getWeight());
       patient.setAge(request.getAge());
       patient.setGender(request.getGender());
       patient.setHeight((request.getHeight()));

       patient.setUser(user);
       user.setPatient(patient);


       Patient createdPatient = patientRepository.save(patient);

       return  entityToDTO(createdPatient);
   }

    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO request)
    {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        patient.setCnp(request.getCnp());
        patient.setWeight(request.getWeight());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setHeight((request.getHeight()));

        User user = patient.getUser();

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail()))
        {
            throw new ResourceAlreadyExists("email already exists");
        }

        if (!patient.getCnp().equals(request.getCnp())
                && patientRepository.existsByCnp(request.getCnp()))
        {
            throw new ResourceAlreadyExists("patient with this CNP already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        Patient updatedPatient = patientRepository.save(patient);

        return entityToDTO(updatedPatient);


    }


   public void deletePatient(Long id)
   {
       if(!patientRepository.existsById(id))
       {
           throw new ResourceNotFoundException("patient not found");
       }
       patientRepository.deleteById(id);
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
