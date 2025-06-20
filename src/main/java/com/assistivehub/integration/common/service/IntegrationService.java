package com.assistivehub.integration.common.service;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.assistivehub.integration.slack.service.SlackIntegrationService;
import com.assistivehub.integration.notion.service.NotionManualSetupService;
import com.assistivehub.repository.NotionIntegrationRepository;
import com.assistivehub.repository.GitHubIntegrationRepository;
import com.assistivehub.repository.GitLabIntegrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IntegrationService {

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Autowired
    private SlackIntegrationService slackIntegrationService;

    @Autowired
    private NotionIntegrationRepository notionIntegrationRepository;

    @Autowired
    private GitHubIntegrationRepository gitHubIntegrationRepository;

    @Autowired
    private GitLabIntegrationRepository gitLabIntegrationRepository;

    /**
     * 사용자의 모든 연동 서비스 조회
     */
    public List<IntegratedService> getUserIntegrations(Long userId) {
        return integratedServiceRepository.findByUserId(userId);
    }

    /**
     * 사용자의 활성 연동 서비스 조회
     */
    public List<IntegratedService> getActiveUserIntegrations(Long userId) {
        return integratedServiceRepository.findActiveByUserId(userId);
    }

    /**
     * 사용자의 연동 통계 조회
     */
    public Map<String, Object> getUserIntegrationStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 전체 연동 수
        long totalIntegrations = integratedServiceRepository.countByUserId(userId);
        long activeIntegrations = integratedServiceRepository.countActiveByUserId(userId);

        // 서비스별 연동 수
        long slackCount = slackIntegrationService.countUserSlackIntegrations(userId);
        long slackActiveCount = slackIntegrationService.countActiveUserSlackIntegrations(userId);
        long notionCount = notionIntegrationRepository.findByUserId(userId).size();
        long notionActiveCount = notionIntegrationRepository.findActiveByUserId(userId).size();
        long githubCount = gitHubIntegrationRepository.findByUserId(userId).size();
        long githubActiveCount = gitHubIntegrationRepository.findActiveByUserId(userId).size();
        long gitlabCount = gitLabIntegrationRepository.findByUserId(userId).size();
        long gitlabActiveCount = gitLabIntegrationRepository.findActiveByUserId(userId).size();

        stats.put("total", totalIntegrations);
        stats.put("active", activeIntegrations);

        Map<String, Object> services = new HashMap<>();

        Map<String, Object> slackStats = new HashMap<>();
        slackStats.put("total", slackCount);
        slackStats.put("active", slackActiveCount);
        services.put("slack", slackStats);

        Map<String, Object> notionStats = new HashMap<>();
        notionStats.put("total", notionCount);
        notionStats.put("active", notionActiveCount);
        services.put("notion", notionStats);

        Map<String, Object> githubStats = new HashMap<>();
        githubStats.put("total", githubCount);
        githubStats.put("active", githubActiveCount);
        services.put("github", githubStats);

        Map<String, Object> gitlabStats = new HashMap<>();
        gitlabStats.put("total", gitlabCount);
        gitlabStats.put("active", gitlabActiveCount);
        services.put("gitlab", gitlabStats);

        stats.put("services", services);

        return stats;
    }

    /**
     * 연동 활성화/비활성화
     */
    public IntegratedService toggleIntegration(Long userId, Long integrationId) {
        IntegratedService integration = integratedServiceRepository
                .findByUserIdAndId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("연동 정보를 찾을 수 없습니다."));

        integration.setIsActive(!integration.getIsActive());
        return integratedServiceRepository.save(integration);
    }

    /**
     * 연동 삭제
     */
    public void deleteIntegration(Long userId, Long integrationId) {
        IntegratedService integration = integratedServiceRepository
                .findByUserIdAndId(userId, integrationId)
                .orElseThrow(() -> new RuntimeException("연동 정보를 찾을 수 없습니다."));

        integratedServiceRepository.delete(integration);
    }
}