package com.assistivehub.integration.slack.service;

import com.assistivehub.integration.slack.dto.SlackIntegrationResponse;
import com.assistivehub.entity.SlackIntegration;
import com.assistivehub.entity.User;
import com.assistivehub.repository.SlackIntegrationRepository;
import com.assistivehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlackIntegrationService {

    @Autowired
    private SlackIntegrationRepository slackIntegrationRepository;

    @Autowired
    private SlackOAuthService slackOAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private SlackManualSetupService slackManualSetupService;

    /**
     * 사용자의 모든 슬랙 연동 조회
     */
    public List<SlackIntegrationResponse> getUserSlackIntegrations(Long userId) {
        List<SlackIntegration> integrations = slackIntegrationRepository.findByUserId(userId);
        return integrations.stream()
                .map(slackManualSetupService::createResponseWithDecryptedData)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 활성 슬랙 연동 조회
     */
    public List<SlackIntegrationResponse> getActiveUserSlackIntegrations(Long userId) {
        List<SlackIntegration> integrations = slackIntegrationRepository.findActiveByUserId(userId);
        return integrations.stream()
                .map(slackManualSetupService::createResponseWithDecryptedData)
                .collect(Collectors.toList());
    }

    /**
     * 특정 슬랙 연동 조회
     */
    public SlackIntegrationResponse getSlackIntegration(Long userId, Long integrationId) {
        SlackIntegration integration = slackIntegrationRepository
                .findByUserIdAndIntegrationId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("슬랙 연동 정보를 찾을 수 없습니다."));

        return slackManualSetupService.createResponseWithDecryptedData(integration);
    }

    /**
     * 슬랙 OAuth 인증 URL 생성
     */
    public String generateSlackAuthUrl(Long userId) {
        // 사용자 ID를 state로 사용하여 콜백에서 식별
        String state = "user_" + userId + "_" + System.currentTimeMillis();
        return slackOAuthService.generateAuthUrl(state);
    }

    /**
     * 슬랙 OAuth 콜백 처리
     */
    @Transactional
    public SlackIntegrationResponse handleSlackCallback(Long userId, String code, String redirectUri) {
        User user = userService.findById(userId);
        SlackIntegration integration = slackOAuthService.exchangeCodeForToken(user, code, redirectUri);
        return new SlackIntegrationResponse(integration);
    }

    /**
     * 슬랙 연동 해제
     */
    @Transactional
    public void disconnectSlackIntegration(Long userId, Long integrationId) {
        SlackIntegration integration = slackIntegrationRepository
                .findByUserIdAndIntegrationId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("슬랙 연동 정보를 찾을 수 없습니다."));

        slackOAuthService.revokeIntegration(userId, integrationId);
    }

    /**
     * 슬랙 연동 상태 확인
     */
    public boolean validateSlackIntegration(Long userId, Long integrationId) {
        SlackIntegration integration = slackIntegrationRepository
                .findByUserIdAndIntegrationId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("슬랙 연동 정보를 찾을 수 없습니다."));

        return slackOAuthService.validateIntegration(integration);
    }

    /**
     * 슬랙 연동 활성화/비활성화
     */
    @Transactional
    public SlackIntegrationResponse toggleSlackIntegration(Long userId, Long integrationId) {
        SlackIntegration integration = slackIntegrationRepository
                .findByUserIdAndIntegrationId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("슬랙 연동 정보를 찾을 수 없습니다."));

        boolean currentStatus = integration.getIntegratedService().getIsActive();
        integration.getIntegratedService().setIsActive(!currentStatus);

        SlackIntegration savedIntegration = slackIntegrationRepository.save(integration);
        return slackManualSetupService.createResponseWithDecryptedData(savedIntegration);
    }

    /**
     * 사용자의 슬랙 연동 개수 조회
     */
    public long countUserSlackIntegrations(Long userId) {
        return slackIntegrationRepository.findByUserId(userId).size();
    }

    /**
     * 사용자의 활성 슬랙 연동 개수 조회
     */
    public long countActiveUserSlackIntegrations(Long userId) {
        return slackIntegrationRepository.findActiveByUserId(userId).size();
    }
}