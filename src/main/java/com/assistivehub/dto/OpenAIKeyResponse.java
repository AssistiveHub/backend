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
     * API 키를 마스킹 처리 (OpenAI 키 형식에 맞게 앞부분만 적절히 보여줌)
     * OpenAI 키 형식: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "sk-****";
        }

        // OpenAI 키는 보통 sk-로 시작하므로 이를 고려
        if (apiKey.startsWith("sk-")) {
            // sk- + 다음 8자리 + **** + 마지막 4자리
            if (apiKey.length() >= 15) {
                String prefix = apiKey.substring(0, 11); // sk- + 8자리
                String suffix = apiKey.substring(apiKey.length() - 4);
                return prefix + "****" + suffix;
            } else {
                // 짧은 키의 경우
                return apiKey.substring(0, Math.min(7, apiKey.length())) + "****";
            }
        } else {
            // sk-로 시작하지 않는 키의 경우 (기존 방식)
            String prefix = apiKey.substring(0, Math.min(6, apiKey.length()));
            String suffix = apiKey.length() > 10 ? apiKey.substring(apiKey.length() - 4) : "";
            return prefix + "****" + suffix;
        }
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