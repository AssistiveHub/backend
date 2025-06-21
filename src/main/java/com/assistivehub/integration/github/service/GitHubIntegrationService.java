package com.assistivehub.integration.github.service;

import com.assistivehub.entity.GitHubIntegration;
import com.assistivehub.entity.IntegratedService;
import com.assistivehub.integration.github.dto.GitHubIntegrationResponse;
import com.assistivehub.repository.GitHubIntegrationRepository;
import com.assistivehub.repository.IntegratedServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GitHubIntegrationService {

    @Autowired
    private GitHubIntegrationRepository gitHubIntegrationRepository;

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    /**
     * 사용자의 모든 GitHub 연동 조회
     */
    @Transactional(readOnly = true)
    public List<GitHubIntegrationResponse> getUserGitHubIntegrations(Long userId) {
        List<GitHubIntegration> integrations = gitHubIntegrationRepository.findByUserId(userId);
        return integrations.stream()
                .map(GitHubIntegrationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 활성 GitHub 연동만 조회
     */
    @Transactional(readOnly = true)
    public List<GitHubIntegrationResponse> getActiveUserGitHubIntegrations(Long userId) {
        List<GitHubIntegration> integrations = gitHubIntegrationRepository.findActiveByUserId(userId);
        return integrations.stream()
                .map(GitHubIntegrationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 GitHub 연동 조회
     */
    @Transactional(readOnly = true)
    public GitHubIntegrationResponse getGitHubIntegration(Long userId, Long integrationId) {
        GitHubIntegration integration = gitHubIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub 연동을 찾을 수 없습니다."));

        // 사용자 소유 확인
        if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        return GitHubIntegrationResponse.fromEntity(integration);
    }

    /**
     * GitHub 연동 해제
     */
    public void disconnectGitHubIntegration(Long userId, Long integrationId) {
        GitHubIntegration integration = gitHubIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub 연동을 찾을 수 없습니다."));

        // 사용자 소유 확인
        if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        // IntegratedService도 함께 삭제
        IntegratedService integratedService = integration.getIntegratedService();
        gitHubIntegrationRepository.delete(integration);
        integratedServiceRepository.delete(integratedService);
    }

    /**
     * GitHub 연동 활성화/비활성화
     */
    public GitHubIntegrationResponse toggleGitHubIntegration(Long userId, Long integrationId) {
        GitHubIntegration integration = gitHubIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub 연동을 찾을 수 없습니다."));

        // 사용자 소유 확인
        if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        IntegratedService integratedService = integration.getIntegratedService();
        integratedService.setIsActive(!integratedService.getIsActive());
        integratedService.setUpdatedAt(LocalDateTime.now());

        integratedServiceRepository.save(integratedService);

        return GitHubIntegrationResponse.fromEntity(integration);
    }

    /**
     * GitHub 연동 유효성 검증
     */
    public boolean validateGitHubIntegration(Long userId, Long integrationId) {
        try {
            GitHubIntegration integration = gitHubIntegrationRepository.findById(integrationId)
                    .orElseThrow(() -> new RuntimeException("GitHub 연동을 찾을 수 없습니다."));

            // 사용자 소유 확인
            if (!integration.getIntegratedService().getUser().getId().equals(userId)) {
                throw new RuntimeException("접근 권한이 없습니다.");
            }

            // TODO: 실제 GitHub API 호출로 토큰 유효성 검증
            // 현재는 단순히 토큰 존재 여부만 확인
            return integration.getAccessToken() != null && !integration.getAccessToken().trim().isEmpty();

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GitHub 연동 통계
     */
    @Transactional(readOnly = true)
    public GitHubIntegrationStats getGitHubIntegrationStats(Long userId) {
        List<GitHubIntegration> allIntegrations = gitHubIntegrationRepository.findByUserId(userId);
        List<GitHubIntegration> activeIntegrations = gitHubIntegrationRepository.findActiveByUserId(userId);

        GitHubIntegrationStats stats = new GitHubIntegrationStats();
        stats.setTotalIntegrations(allIntegrations.size());
        stats.setActiveIntegrations(activeIntegrations.size());
        stats.setInactiveIntegrations(allIntegrations.size() - activeIntegrations.size());

        // 최근 동기화 시간
        allIntegrations.stream()
                .filter(integration -> integration.getLastSyncAt() != null)
                .max((i1, i2) -> i1.getLastSyncAt().compareTo(i2.getLastSyncAt()))
                .ifPresent(integration -> stats.setLastSyncAt(integration.getLastSyncAt()));

        return stats;
    }

    // 통계 클래스
    public static class GitHubIntegrationStats {
        private int totalIntegrations;
        private int activeIntegrations;
        private int inactiveIntegrations;
        private LocalDateTime lastSyncAt;

        // Getters and Setters
        public int getTotalIntegrations() {
            return totalIntegrations;
        }

        public void setTotalIntegrations(int totalIntegrations) {
            this.totalIntegrations = totalIntegrations;
        }

        public int getActiveIntegrations() {
            return activeIntegrations;
        }

        public void setActiveIntegrations(int activeIntegrations) {
            this.activeIntegrations = activeIntegrations;
        }

        public int getInactiveIntegrations() {
            return inactiveIntegrations;
        }

        public void setInactiveIntegrations(int inactiveIntegrations) {
            this.inactiveIntegrations = inactiveIntegrations;
        }

        public LocalDateTime getLastSyncAt() {
            return lastSyncAt;
        }

        public void setLastSyncAt(LocalDateTime lastSyncAt) {
            this.lastSyncAt = lastSyncAt;
        }
    }
}