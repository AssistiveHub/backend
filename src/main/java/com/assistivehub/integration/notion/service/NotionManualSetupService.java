package com.assistivehub.integration.notion.service;

import com.assistivehub.integration.notion.dto.NotionManualSetupRequest;
import com.assistivehub.integration.notion.dto.NotionIntegrationResponse;
import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.NotionIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.NotionIntegrationRepository;
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
public class NotionManualSetupService {

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private NotionIntegrationRepository notionIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NotionManualSetupService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 노션 토큰 유효성 검증
     */
    public boolean validateNotionToken(String token) {
        try {
            String response = webClient.get()
                    .uri("/users/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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

    /**
     * 토큰으로부터 노션 사용자 정보 가져오기
     */
    public NotionUserInfo getNotionUserInfo(String token) {
        try {
            String response = webClient.get()
                    .uri("/users/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("Notion-Version", "2022-06-28")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);

            NotionUserInfo userInfo = new NotionUserInfo();
            userInfo.setBotId(responseJson.get("id").asText());
            userInfo.setBotName(responseJson.get("name").asText());
            userInfo.setOwnerUserId(
                    responseJson.has("owner") ? responseJson.get("owner").get("user").get("id").asText() : null);
            userInfo.setWorkspaceId(
                    responseJson.has("workspace_id") ? responseJson.get("workspace_id").asText() : null);

            return userInfo;

        } catch (Exception e) {
            throw new RuntimeException("노션 토큰 정보를 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 노션 수동 연동 생성
     */
    @Transactional
    public NotionIntegrationResponse createManualNotionIntegration(User user, NotionManualSetupRequest request) {
        // 토큰 유효성 검증
        if (!validateNotionToken(request.getAccessToken())) {
            throw new RuntimeException("유효하지 않은 노션 액세스 토큰입니다.");
        }

        NotionUserInfo tokenInfo = getNotionUserInfo(request.getAccessToken());

        // 기존 연동 확인
        Optional<NotionIntegration> existingIntegration = notionIntegrationRepository
                .findByUserIdAndWorkspaceId(user.getId(), tokenInfo.getWorkspaceId());

        if (existingIntegration.isPresent()) {
            return updateExistingManualIntegration(existingIntegration.get(), request, tokenInfo);
        } else {
            return createNewManualIntegration(user, request, tokenInfo);
        }
    }

    /**
     * 기존 연동 업데이트
     */
    private NotionIntegrationResponse updateExistingManualIntegration(NotionIntegration integration,
            NotionManualSetupRequest request, NotionUserInfo tokenInfo) {

        // 기본 정보 업데이트
        integration.setWorkspaceName(request.getWorkspaceName());
        integration.setAccessToken(request.getAccessToken());
        integration.setBotId(tokenInfo.getBotId());
        integration.setOwnerUserId(tokenInfo.getOwnerUserId());
        integration.setWorkspaceId(tokenInfo.getWorkspaceId());

        // 암호화된 설정 정보 업데이트
        if (request.getDatabases() != null) {
            integration.setEncryptedDatabases(encryptDatabases(request.getDatabases()));
        }
        if (request.getPages() != null) {
            integration.setEncryptedPages(encryptPages(request.getPages()));
        }
        if (request.getTemplates() != null) {
            integration.setEncryptedTemplates(encryptTemplates(request.getTemplates()));
        }
        if (request.getSyncSettings() != null) {
            integration.setEncryptedSyncSettings(encryptSyncSettings(request.getSyncSettings()));
        }

        // 개별 설정 업데이트
        integration.setAutoSyncEnabled(request.getAutoSyncEnabled());
        integration.setSyncIntervalMinutes(request.getSyncIntervalMinutes());
        integration.setNotificationEnabled(request.getNotificationEnabled());
        integration.setTemplateAutoApply(request.getTemplateAutoApply());

        NotionIntegration savedIntegration = notionIntegrationRepository.save(integration);
        return createResponseWithDecryptedData(savedIntegration);
    }

    /**
     * 새 연동 생성
     */
    private NotionIntegrationResponse createNewManualIntegration(User user,
            NotionManualSetupRequest request, NotionUserInfo tokenInfo) {

        // IntegratedService 생성
        IntegratedService integratedService = new IntegratedService();
        integratedService.setUser(user);
        integratedService.setServiceName("Notion");
        integratedService.setIsActive(true);
        integratedService.setCreatedAt(LocalDateTime.now());
        integratedService.setUpdatedAt(LocalDateTime.now());

        IntegratedService savedIntegratedService = integratedServiceRepository.save(integratedService);

        // NotionIntegration 생성
        NotionIntegration integration = new NotionIntegration();
        integration.setIntegratedService(savedIntegratedService);
        integration.setWorkspaceName(request.getWorkspaceName());
        integration.setAccessToken(request.getAccessToken());
        integration.setBotId(tokenInfo.getBotId());
        integration.setWorkspaceId(tokenInfo.getWorkspaceId());
        integration.setOwnerUserId(tokenInfo.getOwnerUserId());
        integration.setDuplicatedTemplateId(request.getDuplicatedTemplateId());

        // 암호화된 설정 정보 저장
        if (request.getDatabases() != null) {
            integration.setEncryptedDatabases(encryptDatabases(request.getDatabases()));
        }
        if (request.getPages() != null) {
            integration.setEncryptedPages(encryptPages(request.getPages()));
        }
        if (request.getTemplates() != null) {
            integration.setEncryptedTemplates(encryptTemplates(request.getTemplates()));
        }
        if (request.getSyncSettings() != null) {
            integration.setEncryptedSyncSettings(encryptSyncSettings(request.getSyncSettings()));
        }

        // 개별 설정
        integration.setAutoSyncEnabled(request.getAutoSyncEnabled());
        integration.setSyncIntervalMinutes(request.getSyncIntervalMinutes());
        integration.setNotificationEnabled(request.getNotificationEnabled());
        integration.setTemplateAutoApply(request.getTemplateAutoApply());

        NotionIntegration savedIntegration = notionIntegrationRepository.save(integration);
        return createResponseWithDecryptedData(savedIntegration);
    }

    /**
     * 복호화된 데이터로 응답 생성
     */
    public NotionIntegrationResponse createResponseWithDecryptedData(NotionIntegration integration) {
        NotionIntegrationResponse response = new NotionIntegrationResponse(integration);

        try {
            // 암호화된 설정들 복호화
            response.setDatabases(decryptDatabases(integration.getEncryptedDatabases()));
            response.setPages(decryptPages(integration.getEncryptedPages()));
            response.setTemplates(decryptTemplates(integration.getEncryptedTemplates()));
            response.setSyncSettings(decryptSyncSettings(integration.getEncryptedSyncSettings()));
        } catch (Exception e) {
            // 복호화 실패 시 기본 정보만 반환
            System.err.println("노션 설정 복호화 실패: " + e.getMessage());
        }

        return response;
    }

    // 암호화/복호화 헬퍼 메서드들
    private String encryptDatabases(List<NotionManualSetupRequest.DatabaseInfo> databases) {
        try {
            String json = objectMapper.writeValueAsString(databases);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("데이터베이스 정보 암호화 실패", e);
        }
    }

    private List<NotionManualSetupRequest.DatabaseInfo> decryptDatabases(String encrypted) {
        try {
            if (encrypted == null)
                return null;
            String json = encryptionUtil.decrypt(encrypted);
            return objectMapper.readValue(json, new TypeReference<List<NotionManualSetupRequest.DatabaseInfo>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private String encryptPages(List<NotionManualSetupRequest.PageInfo> pages) {
        try {
            String json = objectMapper.writeValueAsString(pages);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("페이지 정보 암호화 실패", e);
        }
    }

    private List<NotionManualSetupRequest.PageInfo> decryptPages(String encrypted) {
        try {
            if (encrypted == null)
                return null;
            String json = encryptionUtil.decrypt(encrypted);
            return objectMapper.readValue(json, new TypeReference<List<NotionManualSetupRequest.PageInfo>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private String encryptTemplates(List<NotionManualSetupRequest.TemplateInfo> templates) {
        try {
            String json = objectMapper.writeValueAsString(templates);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("템플릿 정보 암호화 실패", e);
        }
    }

    private List<NotionManualSetupRequest.TemplateInfo> decryptTemplates(String encrypted) {
        try {
            if (encrypted == null)
                return null;
            String json = encryptionUtil.decrypt(encrypted);
            return objectMapper.readValue(json, new TypeReference<List<NotionManualSetupRequest.TemplateInfo>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private String encryptSyncSettings(NotionManualSetupRequest.SyncSettings settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("동기화 설정 암호화 실패", e);
        }
    }

    private NotionManualSetupRequest.SyncSettings decryptSyncSettings(String encrypted) {
        try {
            if (encrypted == null)
                return null;
            String json = encryptionUtil.decrypt(encrypted);
            return objectMapper.readValue(json, NotionManualSetupRequest.SyncSettings.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 노션 사용자 정보 클래스
     */
    public static class NotionUserInfo {
        private String botId;
        private String botName;
        private String ownerUserId;
        private String workspaceId;

        // Getters and Setters
        public String getBotId() {
            return botId;
        }

        public void setBotId(String botId) {
            this.botId = botId;
        }

        public String getBotName() {
            return botName;
        }

        public void setBotName(String botName) {
            this.botName = botName;
        }

        public String getOwnerUserId() {
            return ownerUserId;
        }

        public void setOwnerUserId(String ownerUserId) {
            this.ownerUserId = ownerUserId;
        }

        public String getWorkspaceId() {
            return workspaceId;
        }

        public void setWorkspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
        }
    }
}