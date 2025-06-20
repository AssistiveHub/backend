package com.assistivehub.integration.gitlab.service;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.GitLabIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.GitLabIntegrationRepository;
import com.assistivehub.util.EncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class GitLabOAuthService {

    @Value("${gitlab.client.id:dummy}")
    private String clientId;

    @Value("${gitlab.client.secret:dummy}")
    private String clientSecret;

    @Value("${gitlab.redirect.uri:http://localhost:3000/integrations/gitlab/callback}")
    private String redirectUri;

    @Value("${gitlab.url:https://gitlab.com}")
    private String gitlabUrl;

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private GitLabIntegrationRepository gitLabIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final ObjectMapper objectMapper;

    public GitLabOAuthService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 깃랩 OAuth 인증 URL 생성
     */
    public String generateAuthUrl(String state) {
        String scopes = "read_user,read_repository,read_api";

        return gitlabUrl + "/oauth/authorize" +
                "?client_id=" + clientId +
                "&scope=" + scopes +
                "&redirect_uri=" + redirectUri +
                "&state=" + (state != null ? state : "") +
                "&response_type=code";
    }

    /**
     * OAuth 코드를 액세스 토큰으로 교환
     */
    @Transactional
    public GitLabIntegration exchangeCodeForToken(User user, String code, String redirectUri) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl)
                    .build();

            // 1. 액세스 토큰 요청
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("client_id", clientId);
            tokenRequest.put("client_secret", clientSecret);
            tokenRequest.put("code", code);
            tokenRequest.put("redirect_uri", redirectUri);
            tokenRequest.put("grant_type", "authorization_code");

            String tokenResponse = webClient.post()
                    .uri("/oauth/token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(createFormData(tokenRequest)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode tokenJson = objectMapper.readTree(tokenResponse);

            if (tokenJson.has("error")) {
                throw new RuntimeException("깃랩 토큰 교환 실패: " + tokenJson.get("error_description").asText());
            }

            String accessToken = tokenJson.get("access_token").asText();
            String scope = tokenJson.has("scope") ? tokenJson.get("scope").asText() : "";

            // 2. 사용자 정보 조회
            WebClient apiClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String userInfoResponse = apiClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode userInfoJson = objectMapper.readTree(userInfoResponse);

            String gitlabUserId = userInfoJson.get("id").asText();
            String username = userInfoJson.get("username").asText();
            String avatarUrl = userInfoJson.get("avatar_url").asText();
            String profileUrl = userInfoJson.get("web_url").asText();
            String email = userInfoJson.has("email") && !userInfoJson.get("email").isNull()
                    ? userInfoJson.get("email").asText()
                    : null;

            // 3. 기존 연동 확인
            Optional<GitLabIntegration> existingIntegration = gitLabIntegrationRepository
                    .findByUserIdAndGitlabUserId(user.getId(), gitlabUserId);

            if (existingIntegration.isPresent()) {
                // 기존 연동 업데이트
                return updateExistingIntegration(existingIntegration.get(), accessToken, scope,
                        username, avatarUrl, profileUrl, email);
            }

            // 4. 새 연동 생성
            return createNewIntegration(user, gitlabUserId, username, avatarUrl, profileUrl,
                    email, accessToken, scope);

        } catch (Exception e) {
            throw new RuntimeException("깃랩 OAuth 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 연동 정보 업데이트
     */
    private GitLabIntegration updateExistingIntegration(GitLabIntegration integration,
            String accessToken, String scope, String username, String avatarUrl,
            String profileUrl, String email) {
        try {
            integration.setAccessToken(accessToken);
            integration.setUsername(username);
            integration.setAvatarUrl(avatarUrl);
            integration.setProfileUrl(profileUrl);
            integration.setEmail(email);
            integration.setGitlabUrl(gitlabUrl);
            integration.getIntegratedService().setIsActive(true);
            integration.getIntegratedService().setUpdatedAt(LocalDateTime.now());

            return gitLabIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("깃랩 연동 정보 업데이트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 새 연동 생성
     */
    private GitLabIntegration createNewIntegration(User user, String gitlabUserId,
            String username, String avatarUrl, String profileUrl, String email,
            String accessToken, String scope) {
        try {
            // 1. IntegratedService 생성
            IntegratedService integratedService = new IntegratedService();
            integratedService.setUser(user);
            integratedService.setServiceType(IntegratedService.ServiceType.GITLAB);
            integratedService.setServiceName("GitLab");
            integratedService.setIsActive(true);
            integratedService.setCreatedAt(LocalDateTime.now());
            integratedService.setUpdatedAt(LocalDateTime.now());

            IntegratedService savedService = integratedServiceRepository.save(integratedService);

            // 2. GitLabIntegration 생성
            GitLabIntegration gitLabIntegration = new GitLabIntegration();
            gitLabIntegration.setIntegratedService(savedService);
            gitLabIntegration.setGitlabUserId(gitlabUserId);
            gitLabIntegration.setUsername(username);
            gitLabIntegration.setAccessToken(accessToken);
            gitLabIntegration.setAvatarUrl(avatarUrl);
            gitLabIntegration.setProfileUrl(profileUrl);
            gitLabIntegration.setEmail(email);
            gitLabIntegration.setGitlabUrl(gitlabUrl);

            // 기본 설정
            gitLabIntegration.setAutoSyncEnabled(true);
            gitLabIntegration.setSyncCommits(true);
            gitLabIntegration.setSyncMergeRequests(true);
            gitLabIntegration.setSyncIssues(true);
            gitLabIntegration.setSyncReleases(false);
            gitLabIntegration.setSyncPipelines(false);
            gitLabIntegration.setNotificationEnabled(true);
            gitLabIntegration.setWebhookEnabled(false);

            return gitLabIntegrationRepository.save(gitLabIntegration);

        } catch (Exception e) {
            throw new RuntimeException("깃랩 연동 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연동 해제
     */
    @Transactional
    public void revokeIntegration(Long userId, Long integrationId) {
        try {
            GitLabIntegration integration = gitLabIntegrationRepository.findById(integrationId)
                    .orElseThrow(() -> new RuntimeException("연동을 찾을 수 없습니다."));

            if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
                throw new RuntimeException("권한이 없습니다.");
            }

            // 연동 비활성화
            integration.getIntegratedService().setIsActive(false);
            gitLabIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("깃랩 연동 해제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연동 유효성 검증
     */
    public boolean validateIntegration(GitLabIntegration integration) {
        try {
            WebClient apiClient = WebClient.builder()
                    .baseUrl(integration.getGitlabUrl() + "/api/v4")
                    .build();

            String response = apiClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + integration.getAccessToken())
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
     * Form data 생성 헬퍼
     */
    private org.springframework.util.MultiValueMap<String, String> createFormData(Map<String, String> data) {
        org.springframework.util.LinkedMultiValueMap<String, String> formData = new org.springframework.util.LinkedMultiValueMap<>();
        data.forEach(formData::add);
        return formData;
    }
}