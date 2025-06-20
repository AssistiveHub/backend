package com.assistivehub.integration.github.service;

import com.assistivehub.integration.github.dto.GitHubManualSetupRequest;
import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.GitHubIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.GitHubIntegrationRepository;
import com.assistivehub.util.EncryptionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GitHubManualSetupService {

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private GitHubIntegrationRepository gitHubIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GitHubManualSetupService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 깃허브 토큰 유효성 검증
     */
    public boolean validateGitHubToken(String token) {
        try {
            String response = webClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "token " + token)
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
     * 토큰으로부터 깃허브 사용자 정보 가져오기
     */
    public GitHubUserInfo getGitHubUserInfo(String token) {
        try {
            String response = webClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "token " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);

            GitHubUserInfo userInfo = new GitHubUserInfo();
            userInfo.setGithubUserId(responseJson.get("id").asText());
            userInfo.setUsername(responseJson.get("login").asText());
            userInfo.setAvatarUrl(responseJson.get("avatar_url").asText());
            userInfo.setProfileUrl(responseJson.get("html_url").asText());
            if (responseJson.has("email") && !responseJson.get("email").isNull()) {
                userInfo.setEmail(responseJson.get("email").asText());
            }

            return userInfo;

        } catch (Exception e) {
            throw new RuntimeException("깃허브 토큰 정보를 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 깃허브 수동 연동 생성
     */
    @Transactional
    public GitHubIntegration createManualGitHubIntegration(User user, GitHubManualSetupRequest request) {
        // 토큰 유효성 검증
        if (!validateGitHubToken(request.getAccessToken())) {
            throw new RuntimeException("유효하지 않은 깃허브 액세스 토큰입니다.");
        }

        GitHubUserInfo tokenInfo = getGitHubUserInfo(request.getAccessToken());

        // 기존 연동 확인
        Optional<GitHubIntegration> existingIntegration = gitHubIntegrationRepository
                .findByUserIdAndGithubUserId(user.getId(), tokenInfo.getGithubUserId());

        if (existingIntegration.isPresent()) {
            return updateExistingManualIntegration(existingIntegration.get(), request, tokenInfo);
        } else {
            return createNewManualIntegration(user, request, tokenInfo);
        }
    }

    /**
     * 기존 연동 업데이트
     */
    private GitHubIntegration updateExistingManualIntegration(GitHubIntegration integration,
            GitHubManualSetupRequest request, GitHubUserInfo tokenInfo) {

        // 기본 정보 업데이트
        integration.setUsername(tokenInfo.getUsername());
        integration.setAccessToken(request.getAccessToken());
        integration.setGithubUserId(tokenInfo.getGithubUserId());
        integration.setAvatarUrl(tokenInfo.getAvatarUrl());
        integration.setProfileUrl(tokenInfo.getProfileUrl());
        integration.setEmail(tokenInfo.getEmail());

        // 암호화된 설정 정보 업데이트
        if (request.getRepositories() != null) {
            integration.setEncryptedRepositories(encryptRepositories(request.getRepositories()));
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
        integration.setSyncPullRequests(request.getSyncPullRequests());
        integration.setSyncIssues(request.getSyncIssues());
        integration.setSyncReleases(request.getSyncReleases());
        integration.setNotificationEnabled(request.getNotificationEnabled());
        integration.setWebhookEnabled(request.getWebhookEnabled());

        return gitHubIntegrationRepository.save(integration);
    }

    /**
     * 새 연동 생성
     */
    private GitHubIntegration createNewManualIntegration(User user,
            GitHubManualSetupRequest request, GitHubUserInfo tokenInfo) {

        // IntegratedService 생성
        IntegratedService integratedService = new IntegratedService();
        integratedService.setUser(user);
        integratedService.setServiceName("GitHub");
        integratedService.setIsActive(true);
        integratedService.setCreatedAt(LocalDateTime.now());
        integratedService.setUpdatedAt(LocalDateTime.now());

        IntegratedService savedIntegratedService = integratedServiceRepository.save(integratedService);

        // GitHubIntegration 생성
        GitHubIntegration integration = new GitHubIntegration();
        integration.setIntegratedService(savedIntegratedService);
        integration.setUsername(tokenInfo.getUsername());
        integration.setAccessToken(request.getAccessToken());
        integration.setGithubUserId(tokenInfo.getGithubUserId());
        integration.setAvatarUrl(tokenInfo.getAvatarUrl());
        integration.setProfileUrl(tokenInfo.getProfileUrl());
        integration.setEmail(tokenInfo.getEmail());

        // 암호화된 설정 정보 저장
        if (request.getRepositories() != null) {
            integration.setEncryptedRepositories(encryptRepositories(request.getRepositories()));
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
        integration.setSyncPullRequests(request.getSyncPullRequests());
        integration.setSyncIssues(request.getSyncIssues());
        integration.setSyncReleases(request.getSyncReleases());
        integration.setNotificationEnabled(request.getNotificationEnabled());
        integration.setWebhookEnabled(request.getWebhookEnabled());

        return gitHubIntegrationRepository.save(integration);
    }

    // 암호화/복호화 헬퍼 메서드들
    private String encryptRepositories(List<GitHubManualSetupRequest.RepositoryInfo> repositories) {
        try {
            String json = objectMapper.writeValueAsString(repositories);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("레포지토리 정보 암호화 실패", e);
        }
    }

    private String encryptWebhooks(List<GitHubManualSetupRequest.WebhookInfo> webhooks) {
        try {
            String json = objectMapper.writeValueAsString(webhooks);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("웹훅 정보 암호화 실패", e);
        }
    }

    private String encryptNotificationSettings(GitHubManualSetupRequest.NotificationSettings settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("알림 설정 암호화 실패", e);
        }
    }

    private String encryptSyncSettings(GitHubManualSetupRequest.SyncSettings settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("동기화 설정 암호화 실패", e);
        }
    }

    /**
     * 사용자의 깃허브 연동 목록 조회
     */
    public List<GitHubIntegration> getGitHubIntegrationsByUser(User user, boolean activeOnly) {
        try {
            if (activeOnly) {
                return gitHubIntegrationRepository.findActiveByUserId(user.getId());
            } else {
                return gitHubIntegrationRepository.findByUserId(user.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("깃허브 연동 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 깃허브 연동 조회
     */
    public GitHubIntegration getGitHubIntegrationById(Long userId, Long integrationId) {
        try {
            GitHubIntegration integration = gitHubIntegrationRepository.findById(integrationId)
                    .orElseThrow(() -> new RuntimeException("연동을 찾을 수 없습니다."));

            if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
                throw new RuntimeException("권한이 없습니다.");
            }

            return integration;
        } catch (Exception e) {
            throw new RuntimeException("깃허브 연동 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 깃허브 연동 활성화/비활성화 토글
     */
    @Transactional
    public GitHubIntegration toggleGitHubIntegration(Long userId, Long integrationId) {
        try {
            GitHubIntegration integration = getGitHubIntegrationById(userId, integrationId);

            boolean currentStatus = integration.getIntegratedService().getIsActive();
            integration.getIntegratedService().setIsActive(!currentStatus);
            integration.getIntegratedService().setUpdatedAt(LocalDateTime.now());

            return gitHubIntegrationRepository.save(integration);
        } catch (Exception e) {
            throw new RuntimeException("깃허브 연동 토글 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 깃허브 사용자 정보 클래스
     */
    public static class GitHubUserInfo {
        private String githubUserId;
        private String username;
        private String avatarUrl;
        private String profileUrl;
        private String email;

        // Getters and Setters
        public String getGithubUserId() {
            return githubUserId;
        }

        public void setGithubUserId(String githubUserId) {
            this.githubUserId = githubUserId;
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