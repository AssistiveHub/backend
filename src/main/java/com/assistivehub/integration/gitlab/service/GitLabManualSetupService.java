package com.assistivehub.integration.gitlab.service;

import com.assistivehub.integration.gitlab.dto.GitLabManualSetupRequest;
import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.GitLabIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.GitLabIntegrationRepository;
import com.assistivehub.util.EncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GitLabManualSetupService {

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private GitLabIntegrationRepository gitLabIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final ObjectMapper objectMapper;

    public GitLabManualSetupService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 깃랩 토큰 유효성 검증
     */
    public boolean validateGitLabToken(String token, String gitlabUrl) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.has("id");

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 깃랩 사용자 정보 가져오기
     */
    public GitLabUserInfo getGitLabUserInfo(String token, String gitlabUrl) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);

            GitLabUserInfo userInfo = new GitLabUserInfo();
            userInfo.setGitlabUserId(responseJson.get("id").asText());
            userInfo.setUsername(responseJson.get("username").asText());
            userInfo.setAvatarUrl(responseJson.get("avatar_url").asText());
            userInfo.setProfileUrl(responseJson.get("web_url").asText());
            if (responseJson.has("email") && !responseJson.get("email").isNull()) {
                userInfo.setEmail(responseJson.get("email").asText());
            }

            return userInfo;

        } catch (Exception e) {
            throw new RuntimeException("깃랩 토큰 정보를 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 깃랩 수동 연동 생성
     */
    @Transactional
    public GitLabIntegration createManualGitLabIntegration(User user, GitLabManualSetupRequest request) {
        // 토큰 유효성 검증
        if (!validateGitLabToken(request.getAccessToken(), request.getGitlabUrl())) {
            throw new RuntimeException("유효하지 않은 깃랩 액세스 토큰입니다.");
        }

        GitLabUserInfo tokenInfo = getGitLabUserInfo(request.getAccessToken(), request.getGitlabUrl());

        // 기존 연동 확인
        Optional<GitLabIntegration> existingIntegration = gitLabIntegrationRepository
                .findByUserIdAndGitlabUserId(user.getId(), tokenInfo.getGitlabUserId());

        if (existingIntegration.isPresent()) {
            return updateExistingManualIntegration(existingIntegration.get(), request, tokenInfo);
        } else {
            return createNewManualIntegration(user, request, tokenInfo);
        }
    }

    /**
     * 기존 연동 업데이트
     */
    private GitLabIntegration updateExistingManualIntegration(GitLabIntegration integration,
            GitLabManualSetupRequest request, GitLabUserInfo tokenInfo) {

        // 기본 정보 업데이트
        integration.setUsername(tokenInfo.getUsername());
        integration.setAccessToken(request.getAccessToken());
        integration.setGitlabUserId(tokenInfo.getGitlabUserId());
        integration.setAvatarUrl(tokenInfo.getAvatarUrl());
        integration.setProfileUrl(tokenInfo.getProfileUrl());
        integration.setEmail(tokenInfo.getEmail());
        integration.setGitlabUrl(request.getGitlabUrl());

        // 암호화된 설정 정보 업데이트
        if (request.getProjects() != null) {
            integration.setEncryptedProjects(encryptProjects(request.getProjects()));
        }
        if (request.getWebhooks() != null) {
            integration.setEncryptedWebhooks(encryptWebhooks(request.getWebhooks()));
        }
        if (request.getNotificationSettings() != null) {
            integration
                    .setEncryptedNotificationSettings(encryptNotificationSettings(request.getNotificationSettings()));
        }
        if (request.getSyncSettings() != null) {
            integration.setEncryptedSyncSettings(encryptSyncSettings(request.getSyncSettings()));
        }

        // 개별 설정 업데이트
        integration.setAutoSyncEnabled(request.getAutoSyncEnabled());
        integration.setSyncCommits(request.getSyncCommits());
        integration.setSyncMergeRequests(request.getSyncMergeRequests());
        integration.setSyncIssues(request.getSyncIssues());
        integration.setSyncReleases(request.getSyncReleases());
        integration.setSyncPipelines(request.getSyncPipelines());
        integration.setNotificationEnabled(request.getNotificationEnabled());
        integration.setWebhookEnabled(request.getWebhookEnabled());

        return gitLabIntegrationRepository.save(integration);
    }

    /**
     * 새 연동 생성
     */
    private GitLabIntegration createNewManualIntegration(User user,
            GitLabManualSetupRequest request, GitLabUserInfo tokenInfo) {

        // IntegratedService 생성
        IntegratedService integratedService = new IntegratedService();
        integratedService.setUser(user);
        integratedService.setServiceName("GitLab");
        integratedService.setIsActive(true);
        integratedService.setCreatedAt(LocalDateTime.now());
        integratedService.setUpdatedAt(LocalDateTime.now());

        IntegratedService savedIntegratedService = integratedServiceRepository.save(integratedService);

        // GitLabIntegration 생성
        GitLabIntegration integration = new GitLabIntegration();
        integration.setIntegratedService(savedIntegratedService);
        integration.setUsername(tokenInfo.getUsername());
        integration.setAccessToken(request.getAccessToken());
        integration.setGitlabUserId(tokenInfo.getGitlabUserId());
        integration.setAvatarUrl(tokenInfo.getAvatarUrl());
        integration.setProfileUrl(tokenInfo.getProfileUrl());
        integration.setEmail(tokenInfo.getEmail());
        integration.setGitlabUrl(request.getGitlabUrl());

        // 암호화된 설정 정보 저장
        if (request.getProjects() != null) {
            integration.setEncryptedProjects(encryptProjects(request.getProjects()));
        }
        if (request.getWebhooks() != null) {
            integration.setEncryptedWebhooks(encryptWebhooks(request.getWebhooks()));
        }
        if (request.getNotificationSettings() != null) {
            integration
                    .setEncryptedNotificationSettings(encryptNotificationSettings(request.getNotificationSettings()));
        }
        if (request.getSyncSettings() != null) {
            integration.setEncryptedSyncSettings(encryptSyncSettings(request.getSyncSettings()));
        }

        // 개별 설정
        integration.setAutoSyncEnabled(request.getAutoSyncEnabled());
        integration.setSyncCommits(request.getSyncCommits());
        integration.setSyncMergeRequests(request.getSyncMergeRequests());
        integration.setSyncIssues(request.getSyncIssues());
        integration.setSyncReleases(request.getSyncReleases());
        integration.setSyncPipelines(request.getSyncPipelines());
        integration.setNotificationEnabled(request.getNotificationEnabled());
        integration.setWebhookEnabled(request.getWebhookEnabled());

        return gitLabIntegrationRepository.save(integration);
    }

    // 암호화 헬퍼 메서드들
    private String encryptProjects(Object projects) {
        try {
            String json = objectMapper.writeValueAsString(projects);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("프로젝트 정보 암호화 실패", e);
        }
    }

    private String encryptWebhooks(Object webhooks) {
        try {
            String json = objectMapper.writeValueAsString(webhooks);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("웹훅 정보 암호화 실패", e);
        }
    }

    private String encryptNotificationSettings(Object settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("알림 설정 암호화 실패", e);
        }
    }

    private String encryptSyncSettings(Object settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("동기화 설정 암호화 실패", e);
        }
    }

    /**
     * 깃랩 사용자 정보 클래스
     */
    public static class GitLabUserInfo {
        private String gitlabUserId;
        private String username;
        private String avatarUrl;
        private String profileUrl;
        private String email;

        // Getters and Setters
        public String getGitlabUserId() {
            return gitlabUserId;
        }

        public void setGitlabUserId(String gitlabUserId) {
            this.gitlabUserId = gitlabUserId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}