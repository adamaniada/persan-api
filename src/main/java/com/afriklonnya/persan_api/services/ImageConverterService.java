package com.afriklonnya.persan_api.services;

import com.afriklonnya.persan_api.exceptions.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class ImageConverterService {

    /**
     * Convertit une image en chaîne Base64.
     * @param imageData Les données brutes de l'image (byte array).
     * @return La chaîne Base64 représentant l'image.
     * @throws InvalidRequestException si les données de l'image sont invalides.
     */
    public String convertToBase64(byte[] imageData) throws InvalidRequestException {
        if (imageData == null || imageData.length == 0) {
            throw new InvalidRequestException("Les données de l'image sont vides ou nulles.");
        }
        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * Valide une chaîne Base64 pour s'assurer qu'elle représente une image valide.
     * @param base64String La chaîne Base64 à valider.
     * @throws InvalidRequestException si la chaîne Base64 est invalide.
     */
    public void validateBase64(String base64String) throws InvalidRequestException {
        if (base64String == null || base64String.isEmpty()) {
            throw new InvalidRequestException("La chaîne Base64 est vide ou nulle.");
        }
        try {
            Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("La chaîne Base64 fournie est invalide.");
        }
    }
}