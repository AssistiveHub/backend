package com.assistivehub.repository;

import com.assistivehub.entity.GitLabIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitLabIntegrationRepository extends JpaRepository<GitLabIntegration, Long> {

    /**
     * 특정 사용자의 모든 깃랩 연동 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.integratedService.user.id = :userId")
    List<GitLabIntegration> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 활성 깃랩 연동 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.integratedService.user.id = :userId AND g.integratedService.isActive = true")
    List<GitLabIntegration> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 연동 ID로 깃랩 연동 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.integratedService.user.id = :userId AND g.integratedService.id = :integrationId")
    Optional<GitLabIntegration> findByUserIdAndIntegrationId(@Param("userId") Long userId,
            @Param("integrationId") Long integrationId);

    /**
     * 깃랩 사용자 ID로 연동 조회
     */
    Optional<GitLabIntegration> findByGitlabUserId(String gitlabUserId);

    /**
     * 사용자명으로 연동 조회
     */
    Optional<GitLabIntegration> findByUsername(String username);

    /**
     * 특정 사용자의 특정 깃랩 사용자 연동 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.integratedService.user.id = :userId AND g.gitlabUserId = :gitlabUserId")
    Optional<GitLabIntegration> findByUserIdAndGitlabUserId(@Param("userId") Long userId,
            @Param("gitlabUserId") String gitlabUserId);

    /**
     * 특정 깃랩 URL의 연동들 조회
     */
    List<GitLabIntegration> findByGitlabUrl(String gitlabUrl);

    /**
     * 자동 동기화가 활성화된 연동들 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.autoSyncEnabled = true AND g.integratedService.isActive = true")
    List<GitLabIntegration> findAutoSyncEnabledIntegrations();

    /**
     * 특정 시간 이후 동기화되지 않은 연동들 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.autoSyncEnabled = true AND g.integratedService.isActive = true AND (g.lastSyncAt IS NULL OR g.lastSyncAt < :syncTime)")
    List<GitLabIntegration> findIntegrationsNeedingSync(@Param("syncTime") java.time.LocalDateTime syncTime);

    /**
     * 웹훅이 활성화된 연동들 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.webhookEnabled = true AND g.integratedService.isActive = true")
    List<GitLabIntegration> findWebhookEnabledIntegrations();

    /**
     * 파이프라인 동기화가 활성화된 연동들 조회
     */
    @Query("SELECT g FROM GitLabIntegration g WHERE g.syncPipelines = true AND g.integratedService.isActive = true")
    List<GitLabIntegration> findPipelineSyncEnabledIntegrations();
}