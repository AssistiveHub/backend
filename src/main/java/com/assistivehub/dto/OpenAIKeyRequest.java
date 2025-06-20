package com.assistivehub.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class OpenAIKeyRequest {

    @NotBlank(message = "OpenAI API 키는 필수입니다")
    @Size(min = 10, message = "OpenAI API 키는 최소 10자 이상이어야 합니다")
    private String apiKey;

    @Size(max = 100, message = "키 이름은 100자 이하여야 합니다")
    private String keyName;

    // Constructors
    public OpenAIKeyRequest() {
    }

    public OpenAIKeyRequest(String apiKey, String keyName) {
        this.apiKey = apiKey;
        this.keyName = keyName;
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
}