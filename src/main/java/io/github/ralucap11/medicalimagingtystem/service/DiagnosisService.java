package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.DiagnosisResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Diagnosis;
import io.github.ralucap11.medicalimagingtystem.entity.Doctor;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.DiagnosisRepository;
import io.github.ralucap11.medicalimagingtystem.repository.DoctorRepository;
import io.github.ralucap11.medicalimagingtystem.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public DiagnosisResponseDTO saveDiagnosis(Long patientId, DiagnosisRequestDTO request, String doctorEmail) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("doctor not found"));

        Diagnosis diagnosis = diagnosisRepository.findByPatientId(patientId)
                .orElseGet(Diagnosis::new);

        diagnosis.setTitle(request.getTitle());
        diagnosis.setDescription(request.getDescription());
        diagnosis.setDate(LocalDate.now());
        diagnosis.setPatient(patient);
        diagnosis.setDoctor(doctor);

        return entityToDTO(diagnosisRepository.save(diagnosis));
    }

    public DiagnosisResponseDTO getDiagnosisByPatient(Long patientId) {
        return diagnosisRepository.findByPatientId(patientId)
                .map(this::entityToDTO)
                .orElse(null);   // null când n-a scris nimeni nimic, fără eroare
    }

    private DiagnosisResponseDTO entityToDTO(Diagnosis diagnosis) {
        DiagnosisResponseDTO dto = new DiagnosisResponseDTO();
        dto.setId(diagnosis.getId());
        dto.setTitle(diagnosis.getTitle());
        dto.setDescription(diagnosis.getDescription());
        dto.setDate(diagnosis.getDate());
        dto.setPatientId(diagnosis.getPatient().getId());
        if (diagnosis.getDoctor() != null && diagnosis.getDoctor().getUser() != null) {
            dto.setDoctorFirstName(diagnosis.getDoctor().getUser().getFirstName());
            dto.setDoctorLastName(diagnosis.getDoctor().getUser().getLastName());
        }
        return dto;
    }
}