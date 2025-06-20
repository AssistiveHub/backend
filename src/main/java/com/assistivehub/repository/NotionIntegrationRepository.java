package com.assistivehub.repository;

import com.assistivehub.entity.NotionIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotionIntegrationRepository extends JpaRepository<NotionIntegration, Long> {

    /**
     * 특정 사용자의 모든 노션 연동 조회
     */
    @Query("SELECT n FROM NotionIntegration n WHERE n.integratedService.user.id = :userId")
    List<NotionIntegration> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 활성 노션 연동 조회
     */
    @Query("SELECT n FROM NotionIntegration n WHERE n.integratedService.user.id = :userId AND n.integratedService.isActive = true")
    List<NotionIntegration> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 연동 ID로 노션 연동 조회
     */
    @Query("SELECT n FROM NotionIntegration n WHERE n.integratedService.user.id = :userId AND n.integratedService.id = :integrationId")
    Optional<NotionIntegration> findByUserIdAndIntegrationId(@Param("userId") Long userId,
            @Param("integrationId") Long integrationId);

    /**
     * 워크스페이스 ID로 노션 연동 조회
     */
    Optional<NotionIntegration> findByWorkspaceId(String workspaceId);

    /**
     * 봇 ID로 노션 연동 조회
     */
    Optional<NotionIntegration> findByBotId(String botId);

    /**
     * 특정 사용자의 특정 워크스페이스 연동 조회
     */
    @Query("SELECT n FROM NotionIntegration n WHERE n.integratedService.user.id = :userId AND n.workspaceId = :workspaceId")
    Optional<NotionIntegration> findByUserIdAndWorkspaceId(@Param("userId") Long userId,
            @Param("workspaceId") String workspaceId);

    /**
     * 자동 동기화가 활성화된 연동들 조회
     */
    @Query("SELECT n FROM NotionIntegration n WHERE n.autoSyncEnabled = true AND n.integratedService.isActive = true")
    List<NotionIntegration> findAutoSyncEnabledIntegrations();

    /**
     * 특정 시간 이후 동기화되지 않은 연동들 조회
     */
    @Query("SELECT n FROM NotionIntegration n WHERE n.autoSyncEnabled = true AND n.integratedService.isActive = true AND (n.lastSyncAt IS NULL OR n.lastSyncAt < :syncTime)")
    List<NotionIntegration> findIntegrationsNeedingSync(@Param("syncTime") java.time.LocalDateTime syncTime);
}