package io.github.ralucap11.medicalimagingtystem.controller;

import io.github.ralucap11.medicalimagingtystem.dto.AIPredictionDTO;
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

        // Validate input
        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No image provided."));
        }

        String contentType = image.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
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


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthy = aiService.isHealthy();
        return ResponseEntity.ok(Map.of(
                "aiServiceStatus", healthy ? "UP" : "DOWN",
                "message", healthy
                        ? "AI microservice is running."
                        : "AI microservice is not reachable. Start it with: python app.py"
        ));
    }
}

