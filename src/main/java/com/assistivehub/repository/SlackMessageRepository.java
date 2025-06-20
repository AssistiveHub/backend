package com.assistivehub.repository;

import com.assistivehub.entity.SlackMessage;
import com.assistivehub.entity.SlackIntegration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlackMessageRepository extends JpaRepository<SlackMessage, Long> {

    /**
     * 특정 연동의 메시지 조회 (페이징)
     */
    Page<SlackMessage> findBySlackIntegrationOrderByTimestampDesc(SlackIntegration slackIntegration, Pageable pageable);

    /**
     * 특정 연동의 멘션 메시지 조회 (페이징)
     */
    Page<SlackMessage> findBySlackIntegrationAndIsMentionTrueOrderByTimestampDesc(SlackIntegration slackIntegration,
            Pageable pageable);

    /**
     * 특정 연동의 DM 메시지 조회 (페이징)
     */
    Page<SlackMessage> findBySlackIntegrationAndIsDirectMessageTrueOrderByTimestampDesc(
            SlackIntegration slackIntegration, Pageable pageable);

    /**
     * 사용자 ID로 모든 메시지 조회 (페이징)
     */
    @Query("SELECT m FROM SlackMessage m WHERE m.slackIntegration.integratedService.user.id = :userId ORDER BY m.timestamp DESC")
    Page<SlackMessage> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자 ID로 멘션 메시지 조회 (페이징)
     */
    @Query("SELECT m FROM SlackMessage m WHERE m.slackIntegration.integratedService.user.id = :userId AND m.isMention = true ORDER BY m.timestamp DESC")
    Page<SlackMessage> findMentionsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자 ID로 DM 메시지 조회 (페이징)
     */
    @Query("SELECT m FROM SlackMessage m WHERE m.slackIntegration.integratedService.user.id = :userId AND m.isDirectMessage = true ORDER BY m.timestamp DESC")
    Page<SlackMessage> findDirectMessagesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 기간의 메시지 조회
     */
    @Query("SELECT m FROM SlackMessage m WHERE m.slackIntegration.integratedService.user.id = :userId AND m.timestamp BETWEEN :startDate AND :endDate ORDER BY m.timestamp DESC")
    List<SlackMessage> findByUserIdAndDateRange(@Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 메시지 ID로 중복 확인
     */
    boolean existsBySlackIntegrationAndMessageId(SlackIntegration slackIntegration, String messageId);

    /**
     * 특정 채널의 메시지 조회
     */
    Page<SlackMessage> findBySlackIntegrationAndChannelIdOrderByTimestampDesc(SlackIntegration slackIntegration,
            String channelId, Pageable pageable);

    /**
     * 사용자의 최근 메시지 조회 (제한된 개수)
     */
    @Query("SELECT m FROM SlackMessage m WHERE m.slackIntegration.integratedService.user.id = :userId ORDER BY m.timestamp DESC")
    List<SlackMessage> findRecentMessagesByUserId(@Param("userId") Long userId, Pageable pageable);
}