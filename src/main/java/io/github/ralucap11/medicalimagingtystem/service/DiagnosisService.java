package io.github.ralucap11.medicalimagingtystem.service;


import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Diagnosis;
import io.github.ralucap11.medicalimagingtystem.entity.Doctor;
import io.github.ralucap11.medicalimagingtystem.entity.Xray;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.DiagnosisRepository;
import io.github.ralucap11.medicalimagingtystem.repository.DoctorRepository;
import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import io.github.ralucap11.medicalimagingtystem.repository.XrayRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosisService
{
    private final DiagnosisRepository diagnosisRepository;
    private final XrayRepository xrayRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository, XrayRepository xrayRepository, DoctorRepository doctorRepository, UserRepository userRepository)
    {
        this.diagnosisRepository = diagnosisRepository;
        this.xrayRepository = xrayRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

 public DiagnosisResponseDTO addDiagnosis(Long xrayId, DiagnosisRequestDTO request, String doctorEmail)
 {
     Xray xray = xrayRepository.findById(xrayId)
             .orElseThrow(() -> new ResourceNotFoundException("xray not found"));

     if(diagnosisRepository.findByXrayId(xrayId).isPresent())
     {
         throw new ResourceAlreadyExists("diagnosis already exists for this xray");
     }

     Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
             .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));


     Diagnosis diagnosis = new Diagnosis();
     diagnosis.setTitle(request.getTitle());
     diagnosis.setDescription(request.getDescription());
     diagnosis.setDate(request.getDate());
     diagnosis.setXray(xray);
     diagnosis.setDoctor(doctor);

     return entityToDTO(diagnosisRepository.save(diagnosis));
 }

public DiagnosisResponseDTO updateDiagnosis(Long id, DiagnosisRequestDTO request)
{
    Diagnosis diagnosis = diagnosisRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("diagnosis not found"));

    diagnosis.setTitle(request.getTitle());
    diagnosis.setDescription(request.getDescription());
    diagnosis.setDate(request.getDate() != null ? request.getDate() : diagnosis.getDate());

    return entityToDTO(diagnosisRepository.save(diagnosis));
}

    public DiagnosisResponseDTO getDiagnosisByXray(Long xrayId) {
        Diagnosis diagnosis = diagnosisRepository.findByXrayId(xrayId)
                .orElseThrow(() -> new ResourceNotFoundException("diagnosis not found"));
        return entityToDTO(diagnosis);
    }

    public List<DiagnosisResponseDTO> getDiagnosesByPatient(Long patientId) {
        return diagnosisRepository.findByXrayPatientId(patientId)
                .stream()
                .map(this::entityToDTO)
                .toList();
    }
 private DiagnosisResponseDTO entityToDTO(Diagnosis diagnosis)
 {
     DiagnosisResponseDTO dto = new DiagnosisResponseDTO();
     dto.setId(diagnosis.getId());
     dto.setTitle(diagnosis.getTitle());
     dto.setDescription(diagnosis.getDescription());
     dto.setDate(diagnosis.getDate());
     dto.setXrayId(diagnosis.getXray().getId());
     dto.setXrayName(diagnosis.getXray().getXrayName());
     dto.setDoctorFirstName(diagnosis.getDoctor().getUser().getFirstName());
     dto.setDoctorLastName(diagnosis.getDoctor().getUser().getLastName());
     dto.setAiClassification(diagnosis.getXray().getAiClassification());
     dto.setCobbAngle(diagnosis.getXray().getCobbAngle());
     return dto;

 }

}
