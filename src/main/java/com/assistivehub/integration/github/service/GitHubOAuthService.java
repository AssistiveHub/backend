package com.assistivehub.integration.github.service;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.GitHubIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.GitHubIntegrationRepository;
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
public class GitHubOAuthService {

    @Value("${github.client.id:dummy}")
    private String clientId;

    @Value("${github.client.secret:dummy}")
    private String clientSecret;

    @Value("${github.redirect.uri:http://localhost:3000/integrations/github/callback}")
    private String redirectUri;

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private GitHubIntegrationRepository gitHubIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GitHubOAuthService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://github.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 깃허브 OAuth 인증 URL 생성
     */
    public String generateAuthUrl(String state) {
        String scopes = "repo,user:email,read:org";

        return "https://github.com/login/oauth/authorize" +
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
    public GitHubIntegration exchangeCodeForToken(User user, String code, String redirectUri) {
        try {
            // 1. 액세스 토큰 요청
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("client_id", clientId);
            tokenRequest.put("client_secret", clientSecret);
            tokenRequest.put("code", code);
            tokenRequest.put("redirect_uri", redirectUri);

            String tokenResponse = webClient.post()
                    .uri("/login/oauth/access_token")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(tokenRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode tokenJson = objectMapper.readTree(tokenResponse);

            if (tokenJson.has("error")) {
                throw new RuntimeException("깃허브 토큰 교환 실패: " + tokenJson.get("error_description").asText());
            }

            String accessToken = tokenJson.get("access_token").asText();
            String scope = tokenJson.has("scope") ? tokenJson.get("scope").asText() : "";

            // 2. 사용자 정보 조회
            WebClient apiClient = WebClient.builder()
                    .baseUrl("https://api.github.com")
                    .build();

            String userInfoResponse = apiClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode userInfoJson = objectMapper.readTree(userInfoResponse);

            String githubUserId = userInfoJson.get("id").asText();
            String username = userInfoJson.get("login").asText();
            String avatarUrl = userInfoJson.get("avatar_url").asText();
            String profileUrl = userInfoJson.get("html_url").asText();
            String email = userInfoJson.has("email") && !userInfoJson.get("email").isNull()
                    ? userInfoJson.get("email").asText()
                    : null;

            // 3. 기존 연동 확인
            Optional<GitHubIntegration> existingIntegration = gitHubIntegrationRepository
                    .findByUserIdAndGithubUserId(user.getId(), githubUserId);

            if (existingIntegration.isPresent()) {
                // 기존 연동 업데이트
                return updateExistingIntegration(existingIntegration.get(), accessToken, scope,
                        username, avatarUrl, profileUrl, email);
            }

            // 4. 새 연동 생성
            return createNewIntegration(user, githubUserId, username, avatarUrl, profileUrl,
                    email, accessToken, scope);

        } catch (Exception e) {
            throw new RuntimeException("깃허브 OAuth 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 연동 정보 업데이트
     */
    private GitHubIntegration updateExistingIntegration(GitHubIntegration integration,
            String accessToken, String scope, String username, String avatarUrl,
            String profileUrl, String email) {
        try {
            integration.setAccessToken(accessToken);
            integration.setUsername(username);
            integration.setAvatarUrl(avatarUrl);
            integration.setProfileUrl(profileUrl);
            integration.setEmail(email);
            integration.getIntegratedService().setIsActive(true);
            integration.getIntegratedService().setUpdatedAt(LocalDateTime.now());

            return gitHubIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("깃허브 연동 정보 업데이트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 새 연동 생성
     */
    private GitHubIntegration createNewIntegration(User user, String githubUserId,
            String username, String avatarUrl, String profileUrl, String email,
            String accessToken, String scope) {
        try {
            // 1. IntegratedService 생성
            IntegratedService integratedService = new IntegratedService();
            integratedService.setUser(user);
            integratedService.setServiceName("GitHub");
            integratedService.setIsActive(true);
            integratedService.setCreatedAt(LocalDateTime.now());
            integratedService.setUpdatedAt(LocalDateTime.now());

            IntegratedService savedService = integratedServiceRepository.save(integratedService);

            // 2. GitHubIntegration 생성
            GitHubIntegration gitHubIntegration = new GitHubIntegration();
            gitHubIntegration.setIntegratedService(savedService);
            gitHubIntegration.setGithubUserId(githubUserId);
            gitHubIntegration.setUsername(username);
            gitHubIntegration.setAccessToken(accessToken);
            gitHubIntegration.setAvatarUrl(avatarUrl);
            gitHubIntegration.setProfileUrl(profileUrl);
            gitHubIntegration.setEmail(email);

            // 기본 설정
            gitHubIntegration.setAutoSyncEnabled(true);
            gitHubIntegration.setSyncCommits(true);
            gitHubIntegration.setSyncPullRequests(true);
            gitHubIntegration.setSyncIssues(true);
            gitHubIntegration.setSyncReleases(false);
            gitHubIntegration.setNotificationEnabled(true);
            gitHubIntegration.setWebhookEnabled(false);

            return gitHubIntegrationRepository.save(gitHubIntegration);

        } catch (Exception e) {
            throw new RuntimeException("깃허브 연동 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연동 해제
     */
    @Transactional
    public void revokeIntegration(Long userId, Long integrationId) {
        try {
            GitHubIntegration integration = gitHubIntegrationRepository.findById(integrationId)
                    .orElseThrow(() -> new RuntimeException("연동을 찾을 수 없습니다."));

            if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
                throw new RuntimeException("권한이 없습니다.");
            }

            // 연동 비활성화
            integration.getIntegratedService().setIsActive(false);
            gitHubIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("깃허브 연동 해제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연동 유효성 검증
     */
    public boolean validateIntegration(GitHubIntegration integration) {
        try {
            WebClient apiClient = WebClient.builder()
                    .baseUrl("https://api.github.com")
                    .build();

            String response = apiClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "token " + integration.getAccessToken())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.has("id");

        } catch (Exception e) {
            return false;
        }
    }
}