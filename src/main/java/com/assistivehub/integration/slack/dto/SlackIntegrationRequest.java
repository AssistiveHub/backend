package com.assistivehub.integration.slack.dto;

import javax.validation.constraints.NotBlank;

public class SlackIntegrationRequest {

    @NotBlank(message = "Slack 인증 코드는 필수입니다")
    private String code;

    @NotBlank(message = "리디렉트 URI는 필수입니다")
    private String redirectUri;

    private String state;

    // 생성자
    public SlackIntegrationRequest() {
    }

    public SlackIntegrationRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}