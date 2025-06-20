package com.assistivehub.dto;

import javax.validation.constraints.Size;

public class OpenAIKeyUpdateRequest {

    @Size(min = 10, message = "OpenAI API 키는 최소 10자 이상이어야 합니다")
    private String apiKey; // null이면 키는 변경하지 않음

    @Size(max = 100, message = "키 이름은 100자 이하여야 합니다")
    private String keyName;

    private Boolean isActive;

    // Constructors
    public OpenAIKeyUpdateRequest() {
    }

    public OpenAIKeyUpdateRequest(String apiKey, String keyName, Boolean isActive) {
        this.apiKey = apiKey;
        this.keyName = keyName;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}