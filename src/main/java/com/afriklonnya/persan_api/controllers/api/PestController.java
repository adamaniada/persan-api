package com.afriklonnya.persan_api.controllers.api;

import com.afriklonnya.persan_api.dtos.PestDetectionRequest;
import com.afriklonnya.persan_api.exceptions.InvalidRequestException;
import com.afriklonnya.persan_api.services.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PestController {

    private final GeminiService geminiService;

    public PestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/api/detect-pest-multipart", consumes = {"multipart/form-data"})
    public ResponseEntity<String> detectPestMultipart(@ModelAttribute PestDetectionRequest request) {
        try {
            // Validate that at least one of text or image is provided
            if (request.getText() == null && request.getImage() == null) {
                throw new InvalidRequestException("Au moins un champ 'text' ou 'image' doit être fourni.");
            }

            // Convert image to Base64 if provided
            byte[] imageData = null;
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                imageData = request.getImage().getBytes();
            }

            // Pass the extracted text and/or image to the service
            String jsonResponse = geminiService.generatePestJson(request.getText(), imageData);
            return ResponseEntity.ok(jsonResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne : " + e.getMessage());
        }
    }
}
