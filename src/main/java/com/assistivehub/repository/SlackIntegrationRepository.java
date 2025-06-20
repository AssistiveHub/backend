package com.assistivehub.repository;

import com.assistivehub.entity.SlackIntegration;
import com.assistivehub.entity.IntegratedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackIntegrationRepository extends JpaRepository<SlackIntegration, Long> {

    /**
     * 통합 서비스로 슬랙 연동 조회
     */
    Optional<SlackIntegration> findByIntegratedService(IntegratedService integratedService);

    /**
     * 팀 ID와 슬랙 사용자 ID로 조회
     */
    Optional<SlackIntegration> findByTeamIdAndSlackUserId(String teamId, String slackUserId);

    /**
     * 사용자 ID로 모든 슬랙 연동 조회
     */
    @Query("SELECT s FROM SlackIntegration s WHERE s.integratedService.user.id = :userId ORDER BY s.createdAt DESC")
    List<SlackIntegration> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID로 활성 슬랙 연동 조회
     */
    @Query("SELECT s FROM SlackIntegration s WHERE s.integratedService.user.id = :userId AND s.integratedService.isActive = true ORDER BY s.createdAt DESC")
    List<SlackIntegration> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 특정 팀의 모든 연동 조회
     */
    List<SlackIntegration> findByTeamIdOrderByCreatedAtDesc(String teamId);

    /**
     * 사용자 ID와 연동 ID로 조회
     */
    @Query("SELECT s FROM SlackIntegration s WHERE s.integratedService.user.id = :userId AND s.id = :integrationId")
    Optional<SlackIntegration> findByUserIdAndIntegrationId(@Param("userId") Long userId,
            @Param("integrationId") Long integrationId);
}