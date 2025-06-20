package com.assistivehub.repository;

import com.assistivehub.entity.GitHubIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubIntegrationRepository extends JpaRepository<GitHubIntegration, Long> {

    /**
     * 특정 사용자의 모든 깃허브 연동 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.integratedService.user.id = :userId")
    List<GitHubIntegration> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 활성 깃허브 연동 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.integratedService.user.id = :userId AND g.integratedService.isActive = true")
    List<GitHubIntegration> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 연동 ID로 깃허브 연동 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.integratedService.user.id = :userId AND g.integratedService.id = :integrationId")
    Optional<GitHubIntegration> findByUserIdAndIntegrationId(@Param("userId") Long userId,
            @Param("integrationId") Long integrationId);

    /**
     * 깃허브 사용자 ID로 연동 조회
     */
    Optional<GitHubIntegration> findByGithubUserId(String githubUserId);

    /**
     * 사용자명으로 연동 조회
     */
    Optional<GitHubIntegration> findByUsername(String username);

    /**
     * 특정 사용자의 특정 깃허브 사용자 연동 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.integratedService.user.id = :userId AND g.githubUserId = :githubUserId")
    Optional<GitHubIntegration> findByUserIdAndGithubUserId(@Param("userId") Long userId,
            @Param("githubUserId") String githubUserId);

    /**
     * 자동 동기화가 활성화된 연동들 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.autoSyncEnabled = true AND g.integratedService.isActive = true")
    List<GitHubIntegration> findAutoSyncEnabledIntegrations();

    /**
     * 특정 시간 이후 동기화되지 않은 연동들 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.autoSyncEnabled = true AND g.integratedService.isActive = true AND (g.lastSyncAt IS NULL OR g.lastSyncAt < :syncTime)")
    List<GitHubIntegration> findIntegrationsNeedingSync(@Param("syncTime") java.time.LocalDateTime syncTime);

    /**
     * 웹훅이 활성화된 연동들 조회
     */
    @Query("SELECT g FROM GitHubIntegration g WHERE g.webhookEnabled = true AND g.integratedService.isActive = true")
    List<GitHubIntegration> findWebhookEnabledIntegrations();
}