package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.AIPredictionDTO;
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
    private final String AI_URL = "http://localhost:5001";


    public AIPredictionDTO classify(MultipartFile xrayImage) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(xrayImage.getBytes()) {
            @Override
            public String getFilename() { return xrayImage.getOriginalFilename(); }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<AIPredictionDTO> response = restTemplate.postForEntity(
                AI_URL + "/predict", request, AIPredictionDTO.class
        );

        return response.getBody();
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    AI_URL + "/health", String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            return false;
        }
    }
}
