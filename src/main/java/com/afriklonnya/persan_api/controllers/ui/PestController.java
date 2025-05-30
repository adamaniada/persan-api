package com.afriklonnya.persan_api.controllers.ui;

import com.afriklonnya.persan_api.exceptions.InvalidRequestException;
import com.afriklonnya.persan_api.services.GeminiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller("pestUIController")
public class PestController {

    private final GeminiService geminiService;

    public PestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/detect-pest")
    public String showPestDetectionForm() {
        return "pest/form";
    }

    @PostMapping("/detect-pest")
    public String handlePestDetection(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model) {
        try {
            // Validate that at least one of text or image is provided
            if (text == null && (image == null || image.isEmpty())) {
                throw new InvalidRequestException("Au moins un champ 'text' ou 'image' doit être fourni.");
            }

            // Convert image to Base64 if provided
            byte[] imageData = null;
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                model.addAttribute("imageBase64", imageData); // Add image to model
            }

            // Pass the extracted text and/or image to the service
            String jsonResponse = geminiService.generatePestJson(text, imageData);
            model.addAttribute("result", jsonResponse);
        } catch (InvalidRequestException e) {
            model.addAttribute("error", "Erreur : " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Erreur interne : " + e.getMessage());
        }
        return "pest/form";
    }
}
