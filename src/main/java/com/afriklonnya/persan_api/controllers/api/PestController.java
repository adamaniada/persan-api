package com.afriklonnya.persan_api.controllers.api;

import com.afriklonnya.persan_api.exceptions.InvalidRequestException;
import com.afriklonnya.persan_api.services.GeminiService;
import com.afriklonnya.persan_api.services.ImageConverterService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PestController {

    private final GeminiService geminiService;
    private final ImageConverterService imageConverterService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PestController(GeminiService geminiService, ImageConverterService imageConverterService) {
        this.geminiService = geminiService;
        this.imageConverterService = imageConverterService;
    }

    @PostMapping("/api/detect-pest")
    public ResponseEntity<String> detectPest(@RequestBody String requestBody) {
        try {
            // Parse the incoming JSON
            JsonNode root = mapper.readTree(requestBody);

            // Extract text and image data (both optional)
            String text = root.has("text") ? root.get("text").asText() : null;
            String imageBase64 = root.has("imageBase64") ? root.get("imageBase64").asText() : null;

            // Validate that at least one of text or image is provided
            if (text == null && imageBase64 == null) {
                throw new InvalidRequestException("Au moins un champ 'text' ou 'imageBase64' doit être fourni.");
            }

            // Validate Base64 if provided
            if (imageBase64 != null) {
                imageConverterService.validateBase64(imageBase64);
            }

            // Pass the extracted text and/or image to the service
            String jsonResponse = geminiService.generatePestJson(text, imageBase64);
            return ResponseEntity.ok(jsonResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne : " + e.getMessage());
        }
    }

    @PostMapping(value = "/api/detect-pest-multipart", consumes = {"multipart/form-data"})
    public ResponseEntity<String> detectPestMultipart(
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            // Validate that at least one of text or image is provided
            if (text == null && image == null) {
                throw new InvalidRequestException("Au moins un champ 'text' ou 'image' doit être fourni.");
            }

            // Convert image to Base64 if provided
            String imageBase64 = null;
            if (image != null && !image.isEmpty()) {
                imageBase64 = imageConverterService.convertToBase64(image.getBytes());
            }

            // Pass the extracted text and/or image to the service
            String jsonResponse = geminiService.generatePestJson(text, imageBase64);
            return ResponseEntity.ok(jsonResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne : " + e.getMessage());
        }
    }
}