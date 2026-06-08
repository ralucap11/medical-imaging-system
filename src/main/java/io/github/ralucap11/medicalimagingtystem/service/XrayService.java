package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.AIPredictionDTO;
import io.github.ralucap11.medicalimagingtystem.dto.CobbAngleDTO;
import io.github.ralucap11.medicalimagingtystem.dto.XrayResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import io.github.ralucap11.medicalimagingtystem.entity.Xray;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.PatientRepository;
import io.github.ralucap11.medicalimagingtystem.repository.XrayRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class XrayService
{
    private final PatientRepository patientRepository;
    private final XrayRepository xrayRepository;
    private final ScoliosisAIService scoliosisAIService;

    @Value("${xray.storage.path}")
    private String storagePath;

    public XrayService(PatientRepository patientRepository, XrayRepository xrayRepository, ScoliosisAIService scoliosisAIService)
    {
        this.patientRepository = patientRepository;
        this.xrayRepository = xrayRepository;
        this.scoliosisAIService = scoliosisAIService;
    }

    public List<XrayResponseDTO> getPatientXrays(Long patientId)
    {

        if (!patientRepository.existsById(patientId))
        {
            throw new ResourceNotFoundException("patient not found");
        }

        return xrayRepository.findByPatientId(patientId)
                .stream()
                .map(this::entityToDTO)
                .toList();
    }

    public List<XrayResponseDTO> getAllXrays()
    {
        return xrayRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .toList();
    }


    private String classifyByCobb(double cobbAngle) {
        if (cobbAngle < 10.0)  return "Normal";
        if (cobbAngle < 25.0)  return "Mild";
        if (cobbAngle < 40.0)  return "Moderate";
        return "Severe";
    }

    @Transactional
    public XrayResponseDTO uploadXray(Long patientId, MultipartFile file,
                                      String xrayName, String description) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + extension;

        try {
            Path target = Paths.get(storagePath).resolve(storedName);
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("could not store file", e);
        }

        Xray xray = new Xray();
        xray.setFileName(storedName);
        xray.setFormat(file.getContentType());
        xray.setPatient(patient);
        xray.setXrayName(original != null ? original : storedName);
        xray.setDateUploaded(LocalDate.now());
        xray.setDescription(description);
        patient.getXrays().add(xray);

        // 1. DenseNet — clasificare initiala
        try {
            AIPredictionDTO prediction = scoliosisAIService.classify(file);
            if (prediction != null) {
                xray.setAiClassification(prediction.getPredictedClass());
                xray.setAiConfidence(prediction.getConfidence());
            }
        } catch (Exception e) {
            // AI indisponibil
        }

        try {
            CobbAngleDTO cobb = scoliosisAIService.calculateCobb(file, true);
            if (cobb != null) {
                xray.setCobbVisualization(cobb.getVisualization());
                if (cobb.getCobbAngle() != null) {
                    double angle = cobb.getCobbAngle();
                    if (angle <= 80.0) {
                        xray.setCobbAngle(cobb.getCobbAngle());
                        xray.setAiClassification(classifyByCobb(angle));
                    }
                }
            }
        } catch (Exception e) {
            // Cobb indisponibil — ramane clasificarea DenseNet
        }

        return entityToDTO(xrayRepository.save(xray));
    }


    public XrayResponseDTO getXrayById(Long id)
    {
        Xray xray = xrayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("xray not found"));

        return entityToDTO(xray);
    }
    @Transactional
    public void deleteXray(Long id) throws IOException
    {
        Xray xray = xrayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("xray not found"));

        try
        {
            Path file = Paths.get(storagePath).resolve(xray.getFileName());
            Files.deleteIfExists(file);
        }
        catch (IOException e)
        {
            throw new IOException("exception");
        }

        xrayRepository.deleteById(id);
    }


    private XrayResponseDTO entityToDTO(Xray xray)
    {
        XrayResponseDTO response = new XrayResponseDTO();
        response.setId(xray.getId());
        response.setFormat(xray.getFormat());
        response.setXrayName(xray.getXrayName());
        response.setDescription(xray.getDescription());
        response.setDateUploaded(xray.getDateUploaded());
        response.setFileName(xray.getFileName());

        Patient patient = xray.getPatient();
        response.setPatientId(patient.getId());
        response.setPatientFirstName(patient.getUser().getFirstName());
        response.setPatientLastName(patient.getUser().getLastName());

        response.setAiClassification(xray.getAiClassification());
        response.setAiConfidence(xray.getAiConfidence());
        response.setCobbAngle(xray.getCobbAngle());
        response.setCobbVisualization(xray.getCobbVisualization());

        response.setPatientId(patient.getId());
        response.setPatientFirstName(patient.getUser().getFirstName());
        response.setPatientLastName(patient.getUser().getLastName());

        return response;
    }

}
