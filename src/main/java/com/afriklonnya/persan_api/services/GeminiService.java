package com.afriklonnya.persan_api.services;

import com.afriklonnya.persan_api.config.GeminiConfig;
import com.afriklonnya.persan_api.exceptions.ApiServiceException;
import com.afriklonnya.persan_api.exceptions.GeminiServiceException;
import com.afriklonnya.persan_api.exceptions.InvalidInputException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String CONTENT_TYPE_HEADER = "application/json";
    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String CANDIDATES_FIELD = "candidates";
    private static final String CONTENT_FIELD = "content";
    private static final String PARTS_FIELD = "parts";
    private static final String TEXT_FIELD = "text";
    private static final String ERROR_FIELD = "error";
    private static final String MESSAGE_FIELD = "message";
    private final GeminiConfig config;
    private final ImageConverterService imageConverterService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.prompt.base}")
    private String basePrompt;

    public GeminiService(GeminiConfig config, ImageConverterService imageConverterService) {
        this.config = config;
        this.imageConverterService = imageConverterService;
    }

    private String buildPrompt(String text, String imageBase64) {
        if ((text == null || text.isEmpty()) && (imageBase64 == null || imageBase64.isEmpty())) {
            throw new InvalidInputException("Au moins un texte ou une image doit être fourni.");
        }

        StringBuilder fullPrompt = new StringBuilder(basePrompt);

        try {
            if (text != null && !text.isEmpty()) {
                String escapedText = mapper.writeValueAsString(text);
                fullPrompt.append(String.format("%n%nTexte : %s", escapedText));
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    fullPrompt.append("%nAnalyse également l'image pour compléter ou confirmer les informations.");
                }
            } else if (imageBase64 != null && !imageBase64.isEmpty()) {
                fullPrompt.append("%nAnalyse l'image pour identifier le nuisible et fournir les détails correspondants.");
            }

            return mapper.writeValueAsString(fullPrompt.toString());
        } catch (Exception e) {
            throw new GeminiServiceException("Error building prompt", e);
        }
    }

    private String buildPartsJson(String prompt, String imageBase64) {
        List<String> parts = new ArrayList<>();
        parts.add(String.format("{\"text\": %s}", prompt));
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            parts.add(String.format("""
                    {
                      "inline_data": {
                        "mime_type": "image/jpeg",
                        "data": "%s"
                      }
                    }""", imageBase64));
        }
        return "[" + String.join(",", parts) + "]";
    }

    private JsonNode sendGeminiRequest(String requestBody) {
        logger.debug("Generated requestBody: {}", requestBody);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", CONTENT_TYPE_HEADER)
                    .header(API_KEY_HEADER, config.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (logger.isInfoEnabled()) {
                logger.info("Raw API Response: {}", response.body());
            }

            JsonNode root = mapper.readTree(response.body());

            if (root.has(ERROR_FIELD)) {
                JsonNode errorNode = root.get(ERROR_FIELD);
                String errorMessage = errorNode.has(MESSAGE_FIELD) ? errorNode.get(MESSAGE_FIELD).asText() : "Unknown error";
                throw new ApiServiceException("Erreur de l'API Gemini: " + errorMessage);
            }
            return root;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeminiServiceException("Communication error with Gemini API", e);
        } catch (Exception e) {
            throw new GeminiServiceException("Unexpected error processing Gemini response", e);
        }
    }

    private String extractTextFromResponse(JsonNode root, String rawResponseBody) {
      try {
          if (!root.has(CANDIDATES_FIELD)
              || !root.get(CANDIDATES_FIELD).isArray()
              || root.get(CANDIDATES_FIELD).isEmpty()) {
              throw new ApiServiceException("Réponse API invalide : Aucun candidat. Response: " + rawResponseBody);
          }

          JsonNode candidate = root.get(CANDIDATES_FIELD).get(0);
          if (!candidate.has(CONTENT_FIELD)
              || !candidate.get(CONTENT_FIELD).has(PARTS_FIELD)
              || !candidate.get(CONTENT_FIELD).get(PARTS_FIELD).isArray()
              || candidate.get(CONTENT_FIELD).get(PARTS_FIELD).isEmpty()) {
              throw new ApiServiceException("Structure de contenu invalide. Response: " + rawResponseBody);
          }

          return candidate.get(CONTENT_FIELD).get(PARTS_FIELD).get(0).get(TEXT_FIELD).asText();
      } catch (ApiServiceException e) {
          throw new GeminiServiceException("Error processing response", e);
      }
  }

    public String generatePestJson(String text, byte[] imageData) {
        String imageBase64 = null;
        if (imageData != null && imageData.length > 0) {
            imageBase64 = imageConverterService.convertToBase64(imageData);
        }
        String prompt = buildPrompt(text, imageBase64);
        String partsJson = buildPartsJson(prompt, imageBase64);
        String requestBody = String.format("""
                {
                  "contents": [
                    {
                      "parts": %s
                    }
                  ]
                }""", partsJson);

        JsonNode responseRoot = sendGeminiRequest(requestBody);
        return extractTextFromResponse(responseRoot, responseRoot.toString());
    }
}
