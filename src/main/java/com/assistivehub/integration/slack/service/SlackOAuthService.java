package com.assistivehub.integration.slack.service;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.SlackIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.SlackIntegrationRepository;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SlackOAuthService {

    @Value("${slack.client.id:}")
    private String clientId;

    @Value("${slack.client.secret:}")
    private String clientSecret;

    @Value("${slack.redirect.uri:http://localhost:3000/integrations/slack/callback}")
    private String redirectUri;

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private SlackIntegrationRepository slackIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public SlackOAuthService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 슬랙 OAuth 인증 URL 생성
     */
    public String generateAuthUrl(String state) {
        String scopes = "channels:read,groups:read,im:read,mpim:read,chat:write,users:read,team:read";

        return "https://slack.com/oauth/v2/authorize" +
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
    public SlackIntegration exchangeCodeForToken(User user, String code, String redirectUri) {
        try {
            // 1. 액세스 토큰 요청
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("client_id", clientId);
            tokenRequest.put("client_secret", clientSecret);
            tokenRequest.put("code", code);
            tokenRequest.put("redirect_uri", redirectUri);

            String tokenResponse = webClient.post()
                    .uri("/oauth.v2.access")
                    .body(BodyInserters.fromFormData(createFormData(tokenRequest)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode tokenJson = objectMapper.readTree(tokenResponse);

            if (!tokenJson.get("ok").asBoolean()) {
                throw new RuntimeException("슬랙 토큰 교환 실패: " + tokenJson.get("error").asText());
            }

            // 2. 팀 정보 추출
            JsonNode team = tokenJson.get("team");
            String teamId = team.get("id").asText();
            String teamName = team.get("name").asText();

            // 3. 사용자 정보 추출
            JsonNode authedUser = tokenJson.get("authed_user");
            String slackUserId = authedUser.get("id").asText();
            String accessToken = authedUser.get("access_token").asText();
            String scopes = authedUser.get("scope").asText();

            // 4. 사용자 정보 상세 조회
            String userInfoResponse = webClient.get()
                    .uri("/users.info?user=" + slackUserId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode userInfoJson = objectMapper.readTree(userInfoResponse);
            String slackUserName = "";
            if (userInfoJson.get("ok").asBoolean()) {
                JsonNode userInfo = userInfoJson.get("user");
                slackUserName = userInfo.get("real_name").asText();
                if (slackUserName == null || slackUserName.isEmpty()) {
                    slackUserName = userInfo.get("name").asText();
                }
            }

            // 5. 기존 연동 확인
            SlackIntegration existingIntegration = slackIntegrationRepository
                    .findByTeamIdAndSlackUserId(teamId, slackUserId)
                    .orElse(null);

            if (existingIntegration != null) {
                // 기존 연동 업데이트
                updateExistingIntegration(existingIntegration, accessToken, scopes, slackUserName);
                return existingIntegration;
            }

            // 6. 새 연동 생성
            return createNewIntegration(user, teamId, teamName, slackUserId, slackUserName,
                    accessToken, scopes);

        } catch (Exception e) {
            throw new RuntimeException("슬랙 OAuth 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 연동 정보 업데이트
     */
    private void updateExistingIntegration(SlackIntegration integration, String accessToken,
            String scopes, String slackUserName) {
        try {
            // 토큰 암호화
            String encryptedToken = encryptionUtil.encrypt(accessToken);

            integration.setUserToken(encryptedToken);
            integration.setScopes(scopes);
            integration.setSlackUserName(slackUserName);
            integration.getIntegratedService().setIsActive(true);
            integration.getIntegratedService().setLastSyncAt(LocalDateTime.now());

            slackIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("연동 정보 업데이트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 새 연동 생성
     */
    private SlackIntegration createNewIntegration(User user, String teamId, String teamName,
            String slackUserId, String slackUserName,
            String accessToken, String scopes) {
        try {
            // 1. IntegratedService 생성
            IntegratedService integratedService = new IntegratedService();
            integratedService.setUser(user);
            integratedService.setServiceType(IntegratedService.ServiceType.SLACK);
            integratedService.setServiceName(teamName + " - Slack");
            integratedService.setWorkspaceId(teamId);
            integratedService.setWorkspaceName(teamName);
            integratedService.setAccessToken(encryptionUtil.encrypt(accessToken));
            integratedService.setIsActive(true);
            integratedService.setConnectedAt(LocalDateTime.now());
            integratedService.setLastSyncAt(LocalDateTime.now());

            IntegratedService savedService = integratedServiceRepository.save(integratedService);

            // 2. SlackIntegration 생성
            SlackIntegration slackIntegration = new SlackIntegration();
            slackIntegration.setIntegratedService(savedService);
            slackIntegration.setTeamId(teamId);
            slackIntegration.setTeamName(teamName);
            slackIntegration.setSlackUserId(slackUserId);
            slackIntegration.setSlackUserName(slackUserName);
            slackIntegration.setUserToken(encryptionUtil.encrypt(accessToken));
            slackIntegration.setScopes(scopes);
            slackIntegration.setIsBot(false);
            slackIntegration.setInstalledAt(LocalDateTime.now());

            return slackIntegrationRepository.save(slackIntegration);

        } catch (Exception e) {
            throw new RuntimeException("새 연동 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 슬랙 API 호출을 위한 폼 데이터 생성
     */
    private org.springframework.util.MultiValueMap<String, String> createFormData(Map<String, String> data) {
        org.springframework.util.LinkedMultiValueMap<String, String> formData = new org.springframework.util.LinkedMultiValueMap<>();
        data.forEach(formData::add);
        return formData;
    }

    /**
     * 연동 해제
     */
    @Transactional
    public void revokeIntegration(Long userId, Long integrationId) {
        SlackIntegration integration = slackIntegrationRepository
                .findByUserIdAndIntegrationId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("연동 정보를 찾을 수 없습니다."));

        try {
            // 슬랙 토큰 해제 (선택사항)
            String decryptedToken = encryptionUtil.decrypt(integration.getUserToken());

            webClient.post()
                    .uri("/auth.revoke")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + decryptedToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(); // 비동기 처리 (실패해도 로컬 연동은 해제)

        } catch (Exception e) {
            // 토큰 해제 실패해도 로컬 연동은 해제
            System.err.println("슬랙 토큰 해제 실패 (로컬 연동은 해제됨): " + e.getMessage());
        }

        // 로컬 연동 비활성화
        integration.getIntegratedService().setIsActive(false);
        slackIntegrationRepository.save(integration);
    }

    /**
     * 연동 상태 확인
     */
    public boolean validateIntegration(SlackIntegration integration) {
        try {
            String decryptedToken = encryptionUtil.decrypt(integration.getUserToken());

            String response = webClient.get()
                    .uri("/auth.test")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + decryptedToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.get("ok").asBoolean();

        } catch (Exception e) {
            return false;
        }
    }
}