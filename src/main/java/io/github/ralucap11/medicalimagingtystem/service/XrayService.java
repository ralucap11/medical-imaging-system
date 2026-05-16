package io.github.ralucap11.medicalimagingtystem.service;

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
import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class XrayService
{
    private final PatientRepository patientRepository;
    private final XrayRepository xrayRepository;

    @Value("${xray.storage.path}")
    private String storagePath;

    public XrayService(PatientRepository patientRepository, XrayRepository xrayRepository)
    {
        this.patientRepository = patientRepository;
        this.xrayRepository = xrayRepository;
    }



    public List<XrayResponseDTO> getAllXrays()
    {
        return xrayRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .toList();
    }

    @Transactional
    public XrayResponseDTO uploadXray(Long patientId, MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("file is empty");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("patient not found"));

        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null && original.contains("."))
        {
            extension = original.substring(original.lastIndexOf('.'));
        }

        String storedName = UUID.randomUUID() + extension;

        try
        {
            Path target = Paths.get(storagePath).resolve(storedName);
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        }
        catch (IOException e)
        {
            throw new RuntimeException("could not store file", e);
        }

        Xray xray = new Xray();
        xray.setFileName(storedName);
        xray.setFormat(file.getContentType());
        xray.setPatient(patient);

        return entityToDTO(xrayRepository.save(xray));
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
        response.setFileName(xray.getFileName());

        Patient patient = xray.getPatient();
        response.setPatientId(patient.getId());
        response.setPatientFirstName(patient.getUser().getFirstName());
        response.setPatientLastName(patient.getUser().getLastName());

        return response;

    }

}
