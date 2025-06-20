package com.assistivehub.dto;

import com.assistivehub.entity.OpenAIKey;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class OpenAIKeyResponse {

    private Long id;
    private String keyName;
    private String maskedKey; // 마스킹된 키 (보안상 전체 키는 노출하지 않음)
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public OpenAIKeyResponse() {
    }

    public OpenAIKeyResponse(OpenAIKey openAIKey, String decryptedKey) {
        this.id = openAIKey.getId();
        this.keyName = openAIKey.getKeyName();
        this.maskedKey = maskApiKey(decryptedKey);
        this.isActive = openAIKey.getIsActive();
        this.createdAt = openAIKey.getCreatedAt();
        this.updatedAt = openAIKey.getUpdatedAt();
    }

    /**
     * API 키를 마스킹 처리 (앞 4자리와 뒤 4자리만 보여줌)
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }

        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        int maskedLength = apiKey.length() - 8;

        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < maskedLength; i++) {
            masked.append("*");
        }

        return prefix + masked.toString() + suffix;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getMaskedKey() {
        return maskedKey;
    }

    public void setMaskedKey(String maskedKey) {
        this.maskedKey = maskedKey;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}