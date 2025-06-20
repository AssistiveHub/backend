package com.assistivehub.integration.notion.service;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.NotionIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.NotionIntegrationRepository;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NotionOAuthService {

    @Value("${notion.client.id:dummy}")
    private String clientId;

    @Value("${notion.client.secret:dummy}")
    private String clientSecret;

    @Value("${notion.redirect.uri:http://localhost:3000/integrations/notion/callback}")
    private String redirectUri;

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private NotionIntegrationRepository notionIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NotionOAuthService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 노션 OAuth 인증 URL 생성
     */
    public String generateAuthUrl(String state) {
        return "https://api.notion.com/v1/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&owner=user" +
                "&state=" + (state != null ? state : "");
    }

    /**
     * OAuth 코드를 액세스 토큰으로 교환
     */
    @Transactional
    public NotionIntegration exchangeCodeForToken(User user, String code, String redirectUri) {
        try {
            // 1. 액세스 토큰 요청
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "authorization_code");
            tokenRequest.put("code", code);
            tokenRequest.put("redirect_uri", redirectUri);

            // Basic Auth 헤더 생성
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            String tokenResponse = webClient.post()
                    .uri("/oauth/token")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(tokenRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode tokenJson = objectMapper.readTree(tokenResponse);

            if (tokenJson.has("error")) {
                throw new RuntimeException("노션 토큰 교환 실패: " + tokenJson.get("error").asText());
            }

            String accessToken = tokenJson.get("access_token").asText();
            String botId = tokenJson.get("bot_id").asText();

            // 2. 워크스페이스 정보 추출
            JsonNode workspace = tokenJson.get("workspace");
            String workspaceId = workspace.get("id").asText();
            String workspaceName = workspace.get("name").asText();
            String workspaceIcon = workspace.has("icon") ? workspace.get("icon").asText() : null;

            // 3. 봇 정보 추출
            JsonNode owner = tokenJson.get("owner");
            String ownerId = owner.get("user").get("id").asText();
            String ownerName = owner.get("user").get("name").asText();
            String ownerEmail = owner.get("user").has("person") &&
                    owner.get("user").get("person").has("email")
                            ? owner.get("user").get("person").get("email").asText()
                            : null;

            // 4. 기존 연동 확인
            Optional<NotionIntegration> existingIntegration = notionIntegrationRepository
                    .findByUserIdAndWorkspaceId(user.getId(), workspaceId);

            if (existingIntegration.isPresent()) {
                // 기존 연동 업데이트
                return updateExistingIntegration(existingIntegration.get(), accessToken, botId,
                        workspaceName, workspaceIcon, ownerId, ownerName, ownerEmail);
            }

            // 5. 새 연동 생성
            return createNewIntegration(user, workspaceId, workspaceName, workspaceIcon,
                    accessToken, botId, ownerId, ownerName, ownerEmail);

        } catch (Exception e) {
            throw new RuntimeException("노션 OAuth 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 연동 정보 업데이트
     */
    private NotionIntegration updateExistingIntegration(NotionIntegration integration,
            String accessToken, String botId, String workspaceName, String workspaceIcon,
            String ownerId, String ownerName, String ownerEmail) {
        try {
            integration.setAccessToken(accessToken);
            integration.setBotId(botId);
            integration.setWorkspaceName(workspaceName);
            integration.setWorkspaceIcon(workspaceIcon);
            integration.setOwnerId(ownerId);
            integration.setOwnerName(ownerName);
            integration.setOwnerEmail(ownerEmail);
            integration.getIntegratedService().setIsActive(true);
            integration.getIntegratedService().setUpdatedAt(LocalDateTime.now());

            return notionIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("노션 연동 정보 업데이트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 새 연동 생성
     */
    private NotionIntegration createNewIntegration(User user, String workspaceId,
            String workspaceName, String workspaceIcon, String accessToken, String botId,
            String ownerId, String ownerName, String ownerEmail) {
        try {
            // 1. IntegratedService 생성
            IntegratedService integratedService = new IntegratedService();
            integratedService.setUser(user);
            integratedService.setServiceType(IntegratedService.ServiceType.NOTION);
            integratedService.setServiceName("Notion");
            integratedService.setIsActive(true);
            integratedService.setCreatedAt(LocalDateTime.now());
            integratedService.setUpdatedAt(LocalDateTime.now());

            IntegratedService savedService = integratedServiceRepository.save(integratedService);

            // 2. NotionIntegration 생성
            NotionIntegration notionIntegration = new NotionIntegration();
            notionIntegration.setIntegratedService(savedService);
            notionIntegration.setWorkspaceId(workspaceId);
            notionIntegration.setWorkspaceName(workspaceName);
            notionIntegration.setWorkspaceIcon(workspaceIcon);
            notionIntegration.setAccessToken(accessToken);
            notionIntegration.setBotId(botId);
            notionIntegration.setOwnerId(ownerId);
            notionIntegration.setOwnerName(ownerName);
            notionIntegration.setOwnerEmail(ownerEmail);

            // 기본 설정
            notionIntegration.setAutoSyncEnabled(true);
            notionIntegration.setSyncDatabases(true);
            notionIntegration.setSyncPages(true);
            notionIntegration.setSyncBlocks(false);
            notionIntegration.setNotificationEnabled(true);
            notionIntegration.setBidirectionalSync(false);

            return notionIntegrationRepository.save(notionIntegration);

        } catch (Exception e) {
            throw new RuntimeException("노션 연동 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연동 해제
     */
    @Transactional
    public void revokeIntegration(Long userId, Long integrationId) {
        try {
            NotionIntegration integration = notionIntegrationRepository.findById(integrationId)
                    .orElseThrow(() -> new RuntimeException("연동을 찾을 수 없습니다."));

            if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
                throw new RuntimeException("권한이 없습니다.");
            }

            // 연동 비활성화
            integration.getIntegratedService().setIsActive(false);
            notionIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("노션 연동 해제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연동 유효성 검증
     */
    public boolean validateIntegration(NotionIntegration integration) {
        try {
            String response = webClient.get()
                    .uri("/users/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + integration.getAccessToken())
                    .header("Notion-Version", "2022-06-28")
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