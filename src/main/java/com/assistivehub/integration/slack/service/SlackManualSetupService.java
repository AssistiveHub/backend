package com.assistivehub.integration.slack.service;

import com.assistivehub.integration.slack.dto.SlackManualSetupRequest;
import com.assistivehub.integration.slack.dto.SlackIntegrationResponse;
import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.SlackIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.repository.SlackIntegrationRepository;
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
public class SlackManualSetupService {

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private SlackIntegrationRepository slackIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public SlackManualSetupService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 채널 정보를 JSON으로 암호화
     */
    private String encryptChannelInfo(List<SlackManualSetupRequest.ChannelInfo> channels) {
        try {
            if (channels == null || channels.isEmpty()) {
                return null;
            }
            String json = objectMapper.writeValueAsString(channels);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("채널 정보 암호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 암호화된 채널 정보를 복호화
     */
    private List<SlackManualSetupRequest.ChannelInfo> decryptChannelInfo(String encryptedChannels) {
        try {
            if (encryptedChannels == null || encryptedChannels.trim().isEmpty()) {
                return null;
            }
            String json = encryptionUtil.decrypt(encryptedChannels);
            return objectMapper.readValue(json, new TypeReference<List<SlackManualSetupRequest.ChannelInfo>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("채널 정보 복호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 알림 설정을 JSON으로 암호화
     */
    private String encryptNotificationSettings(SlackManualSetupRequest.NotificationSettings settings) {
        try {
            if (settings == null) {
                return null;
            }
            String json = objectMapper.writeValueAsString(settings);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("알림 설정 암호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 암호화된 알림 설정을 복호화
     */
    private SlackManualSetupRequest.NotificationSettings decryptNotificationSettings(String encryptedSettings) {
        try {
            if (encryptedSettings == null || encryptedSettings.trim().isEmpty()) {
                return null;
            }
            String json = encryptionUtil.decrypt(encryptedSettings);
            return objectMapper.readValue(json, SlackManualSetupRequest.NotificationSettings.class);
        } catch (Exception e) {
            throw new RuntimeException("알림 설정 복호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 키워드 목록을 JSON으로 암호화
     */
    private String encryptKeywords(List<String> keywords) {
        try {
            if (keywords == null || keywords.isEmpty()) {
                return null;
            }
            String json = objectMapper.writeValueAsString(keywords);
            return encryptionUtil.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("키워드 암호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 암호화된 키워드 목록을 복호화
     */
    private List<String> decryptKeywords(String encryptedKeywords) {
        try {
            if (encryptedKeywords == null || encryptedKeywords.trim().isEmpty()) {
                return null;
            }
            String json = encryptionUtil.decrypt(encryptedKeywords);
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("키워드 복호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 토큰 유효성 검증
     */
    public boolean validateSlackToken(String token) {
        try {
            String response = webClient.get()
                    .uri("/auth.test")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.get("ok").asBoolean();

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 사용자 정보 가져오기
     */
    public SlackUserInfo getSlackUserInfo(String token) {
        try {
            String response = webClient.get()
                    .uri("/auth.test")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);

            if (!responseJson.get("ok").asBoolean()) {
                throw new RuntimeException("슬랙 토큰이 유효하지 않습니다.");
            }

            SlackUserInfo userInfo = new SlackUserInfo();
            userInfo.setUserId(responseJson.get("user_id").asText());
            userInfo.setTeamId(responseJson.get("team_id").asText());
            userInfo.setTeamName(responseJson.get("team").asText());

            // 사용자 상세 정보 조회
            String userDetailResponse = webClient.get()
                    .uri("/users.info?user=" + userInfo.getUserId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode userDetailJson = objectMapper.readTree(userDetailResponse);
            if (userDetailJson.get("ok").asBoolean()) {
                JsonNode user = userDetailJson.get("user");
                userInfo.setUserName(user.get("real_name").asText());
                if (userInfo.getUserName() == null || userInfo.getUserName().isEmpty()) {
                    userInfo.setUserName(user.get("name").asText());
                }
            }

            return userInfo;

        } catch (Exception e) {
            throw new RuntimeException("슬랙 사용자 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 수동 슬랙 연동 생성
     */
    @Transactional
    public SlackIntegrationResponse createManualSlackIntegration(User user, SlackManualSetupRequest request) {
        try {
            // 1. 토큰 유효성 검증
            if (!validateSlackToken(request.getUserToken())) {
                throw new RuntimeException("제공된 슬랙 토큰이 유효하지 않습니다.");
            }

            // 2. 토큰에서 실제 정보 가져오기 (검증용)
            SlackUserInfo tokenInfo = getSlackUserInfo(request.getUserToken());

            // 3. 사용자가 입력한 정보와 토큰 정보 비교
            if (!tokenInfo.getUserId().equals(request.getSlackUserId())) {
                throw new RuntimeException("토큰의 사용자 ID와 입력한 사용자 ID가 일치하지 않습니다.");
            }

            if (!tokenInfo.getTeamId().equals(request.getTeamId())) {
                throw new RuntimeException("토큰의 팀 ID와 입력한 팀 ID가 일치하지 않습니다.");
            }

            // 4. 기존 연동 확인
            Optional<SlackIntegration> existingIntegration = slackIntegrationRepository
                    .findByTeamIdAndSlackUserId(request.getTeamId(), request.getSlackUserId());

            if (existingIntegration.isPresent()) {
                // 기존 연동 업데이트
                return updateExistingManualIntegration(existingIntegration.get(), request, tokenInfo);
            }

            // 5. 새 연동 생성
            return createNewManualIntegration(user, request, tokenInfo);

        } catch (Exception e) {
            throw new RuntimeException("슬랙 수동 연동 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 수동 연동 업데이트
     */
    private SlackIntegrationResponse updateExistingManualIntegration(SlackIntegration integration,
            SlackManualSetupRequest request,
            SlackUserInfo tokenInfo) {
        try {
            // 토큰 및 설정 업데이트
            integration.setUserToken(encryptionUtil.encrypt(request.getUserToken()));

            if (request.getBotToken() != null && !request.getBotToken().trim().isEmpty()) {
                integration.setBotToken(encryptionUtil.encrypt(request.getBotToken()));
            }

            if (request.getWebhookUrl() != null && !request.getWebhookUrl().trim().isEmpty()) {
                integration.setWebhookUrl(request.getWebhookUrl());
            }

            integration.setSlackUserName(
                    request.getSlackUserName() != null ? request.getSlackUserName() : tokenInfo.getUserName());
            integration.setTeamName(request.getTeamName() != null ? request.getTeamName() : tokenInfo.getTeamName());

            // IntegratedService 업데이트
            IntegratedService service = integration.getIntegratedService();
            service.setServiceName(request.getWorkspaceName());
            service.setWorkspaceName(request.getWorkspaceName());
            service.setAccessToken(encryptionUtil.encrypt(request.getUserToken()));
            service.setIsActive(true);
            service.setLastSyncAt(LocalDateTime.now());

            // 설정 정보 업데이트
            integration.setMonitoringChannels(encryptChannelInfo(request.getMonitoringChannels()));
            integration.setNotificationSettings(encryptNotificationSettings(request.getNotificationSettings()));

            // 키워드 처리
            List<String> keywords = null;
            if (request.getNotificationSettings() != null && request.getNotificationSettings().getKeywords() != null) {
                keywords = request.getNotificationSettings().getKeywords();
            }
            integration.setKeywords(encryptKeywords(keywords));

            // 개별 알림 설정
            if (request.getNotificationSettings() != null) {
                integration.setEnableMentions(request.getNotificationSettings().getEnableMentions());
                integration.setEnableDirectMessages(request.getNotificationSettings().getEnableDirectMessages());
                integration.setEnableChannelMessages(request.getNotificationSettings().getEnableChannelMessages());
                integration.setEnableThreadReplies(request.getNotificationSettings().getEnableThreadReplies());
            }

            SlackIntegration savedIntegration = slackIntegrationRepository.save(integration);
            return createResponseWithDecryptedData(savedIntegration);

        } catch (Exception e) {
            throw new RuntimeException("기존 연동 업데이트 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 새 수동 연동 생성
     */
    private SlackIntegrationResponse createNewManualIntegration(User user,
            SlackManualSetupRequest request,
            SlackUserInfo tokenInfo) {
        try {
            // 1. IntegratedService 생성
            IntegratedService integratedService = new IntegratedService();
            integratedService.setUser(user);
            integratedService.setServiceType(IntegratedService.ServiceType.SLACK);
            integratedService.setServiceName(request.getWorkspaceName());
            integratedService.setWorkspaceId(request.getTeamId());
            integratedService.setWorkspaceName(request.getWorkspaceName());
            integratedService.setAccessToken(encryptionUtil.encrypt(request.getUserToken()));
            integratedService.setIsActive(true);
            integratedService.setConnectedAt(LocalDateTime.now());
            integratedService.setLastSyncAt(LocalDateTime.now());

            IntegratedService savedService = integratedServiceRepository.save(integratedService);

            // 2. SlackIntegration 생성
            SlackIntegration slackIntegration = new SlackIntegration();
            slackIntegration.setIntegratedService(savedService);
            slackIntegration.setTeamId(request.getTeamId());
            slackIntegration
                    .setTeamName(request.getTeamName() != null ? request.getTeamName() : tokenInfo.getTeamName());
            slackIntegration.setSlackUserId(request.getSlackUserId());
            slackIntegration.setSlackUserName(
                    request.getSlackUserName() != null ? request.getSlackUserName() : tokenInfo.getUserName());
            slackIntegration.setUserToken(encryptionUtil.encrypt(request.getUserToken()));

            if (request.getBotToken() != null && !request.getBotToken().trim().isEmpty()) {
                slackIntegration.setBotToken(encryptionUtil.encrypt(request.getBotToken()));
                slackIntegration.setIsBot(true);
            } else {
                slackIntegration.setIsBot(false);
            }

            if (request.getWebhookUrl() != null && !request.getWebhookUrl().trim().isEmpty()) {
                slackIntegration.setWebhookUrl(request.getWebhookUrl());
            }

            slackIntegration.setScopes("manually_configured");
            slackIntegration.setInstalledAt(LocalDateTime.now());

            // 설정 정보 암호화해서 저장
            slackIntegration.setMonitoringChannels(encryptChannelInfo(request.getMonitoringChannels()));
            slackIntegration.setNotificationSettings(encryptNotificationSettings(request.getNotificationSettings()));

            // 키워드 처리
            List<String> keywords = null;
            if (request.getNotificationSettings() != null && request.getNotificationSettings().getKeywords() != null) {
                keywords = request.getNotificationSettings().getKeywords();
            }
            slackIntegration.setKeywords(encryptKeywords(keywords));

            // 개별 알림 설정
            if (request.getNotificationSettings() != null) {
                slackIntegration.setEnableMentions(request.getNotificationSettings().getEnableMentions());
                slackIntegration.setEnableDirectMessages(request.getNotificationSettings().getEnableDirectMessages());
                slackIntegration.setEnableChannelMessages(request.getNotificationSettings().getEnableChannelMessages());
                slackIntegration.setEnableThreadReplies(request.getNotificationSettings().getEnableThreadReplies());
            }

            SlackIntegration savedIntegration = slackIntegrationRepository.save(slackIntegration);
            return createResponseWithDecryptedData(savedIntegration);

        } catch (Exception e) {
            throw new RuntimeException("새 연동 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 복호화된 설정 정보를 포함한 응답 생성
     */
    public SlackIntegrationResponse createResponseWithDecryptedData(SlackIntegration integration) {
        SlackIntegrationResponse response = new SlackIntegrationResponse(integration);

        try {
            // 암호화된 데이터 복호화해서 응답에 추가
            response.setMonitoringChannels(decryptChannelInfo(integration.getMonitoringChannels()));
            response.setNotificationSettings(decryptNotificationSettings(integration.getNotificationSettings()));
            response.setKeywords(decryptKeywords(integration.getKeywords()));

        } catch (Exception e) {
            // 복호화 실패해도 기본 정보는 반환
            System.err.println("설정 정보 복호화 실패: " + e.getMessage());
        }

        return response;
    }

    /**
     * 슬랙 사용자 정보 DTO
     */
    public static class SlackUserInfo {
        private String userId;
        private String userName;
        private String teamId;
        private String teamName;

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getTeamId() {
            return teamId;
        }

        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }
    }
}