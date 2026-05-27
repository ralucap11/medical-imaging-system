package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.AIPredictionDTO;
import io.github.ralucap11.medicalimagingtystem.dto.CobbAngleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;


@Service
public class ScoliosisAIService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url}")
    private String AI_URL;  // ConvNeXt — severitate

    @Value("${ai.service.cobb.url}")
    private String COBB_URL;  // YOLOv8   — unghi Cobb

    private ByteArrayResource toResource(MultipartFile file) throws IOException {
        final String filename = file.getOriginalFilename();
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return filename; }
        };
    }



    public AIPredictionDTO classify(MultipartFile xrayImage) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", toResource(xrayImage));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<AIPredictionDTO> response = restTemplate.postForEntity(
                AI_URL + "/predict", request, AIPredictionDTO.class
        );
        return response.getBody();
    }



    /**
     * @param withVisualization  dacă true, răspunsul include câmpul
     *                           "visualization" — PNG base64 cu liniile
     *                           Cobb desenate pe radiografie.
     *                           Pune false când nu afișezi imaginea
     *                           (economisești ~200–500 KB per apel).
     */
    public CobbAngleDTO calculateCobb(MultipartFile xrayImage,
                                      boolean withVisualization) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", toResource(xrayImage));
        body.add("visualization", String.valueOf(withVisualization));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<CobbAngleDTO> response = restTemplate.postForEntity(
                COBB_URL + "/predict-cobb", request, CobbAngleDTO.class
        );
        return response.getBody();
    }



    public boolean isHealthy() {
        return checkHealth(AI_URL + "/health");
    }

    public boolean isCobbHealthy() {
        return checkHealth(COBB_URL + "/health");
    }

    private boolean checkHealth(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            return false;
        }
    }
}

