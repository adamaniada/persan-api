package com.afriklonnya.persan_api.dtos;

import org.springframework.web.multipart.MultipartFile;

public class PestDetectionRequest {
    private String text;
    private MultipartFile image;

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
