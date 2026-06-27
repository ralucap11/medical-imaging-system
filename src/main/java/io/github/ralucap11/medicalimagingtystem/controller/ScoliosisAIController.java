package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.AIPredictionDTO;
import io.github.ralucap11.medicalimagingtystem.dto.CobbAngleDTO;
import io.github.ralucap11.medicalimagingtystem.service.ScoliosisAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;



@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class ScoliosisAIController {

    private static final Logger logger = LoggerFactory.getLogger(ScoliosisAIController.class);

    private final ScoliosisAIService aiService;

    public ScoliosisAIController(ScoliosisAIService aiService) {
        this.aiService = aiService;
    }



    @PostMapping(value = "/classify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> classify(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No image provided."));
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be an image (JPEG or PNG)."));
        }
        try {
            AIPredictionDTO prediction = aiService.classify(image);
            if (prediction == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "AI service returned empty response."));
            }
            logger.info("Classification result: {}", prediction);
            return ResponseEntity.ok(prediction);
        } catch (IOException e) {
            logger.error("Failed to read image file", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Could not read image: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("AI classification failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI service unavailable. Please ensure the Python service is running."));
        }
    }



    @PostMapping(value = "/cobb-angle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> cobbAngle(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "visualization", defaultValue = "true") boolean withVisualization) {

        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No image provided."));
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be an image (JPEG or PNG)."));
        }
        try {
            CobbAngleDTO result = aiService.calculateCobb(image, withVisualization);
            if (result == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Cobb service returned empty response."));
            }
            logger.info("Cobb angle result: {}° ({})", result.getCobbAngle(), result.getSeverity());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Failed to read image file", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Could not read image: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Cobb angle calculation failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Cobb service unavailable. Please ensure the Python service is running on port 5002."));
        }
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean classifierUp = aiService.isHealthy();
        boolean cobbUp       = aiService.isCobbHealthy();
        return ResponseEntity.ok(Map.of(
                "classifierService", classifierUp ? "UP" : "DOWN",
                "classifierMessage", classifierUp
                        ? "DenseNet-121 service is running (port 5001)."
                        : "DenseNet-121 service is not reachable. Start with: python app.py",
                "cobbService",       cobbUp ? "UP" : "DOWN",
                "cobbMessage",       cobbUp
                        ? "Cobb service is running (port 5002)."
                        : "Cobb service is not reachable. Start with: python app_cobb.py"
        ));
    }
}

