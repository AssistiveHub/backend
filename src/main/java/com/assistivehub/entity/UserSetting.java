package com.assistivehub.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 주간 업무일지 작성 on/off
    @NotNull
    @Column(name = "weekly_work_log_enabled", nullable = false)
    private Boolean weeklyWorkLogEnabled = true;

    // 일일 업무일지 on/off
    @NotNull
    @Column(name = "daily_work_log_enabled", nullable = false)
    private Boolean dailyWorkLogEnabled = true;

    // 업무 피드백 및 리뷰 관리 on/off
    @NotNull
    @Column(name = "feedback_review_enabled", nullable = false)
    private Boolean feedbackReviewEnabled = true;

    // 스킬 성장 측정 on/off
    @NotNull
    @Column(name = "skill_growth_tracking_enabled", nullable = false)
    private Boolean skillGrowthTrackingEnabled = true;

    // 알람 정돈 on/off
    @NotNull
    @Column(name = "notification_management_enabled", nullable = false)
    private Boolean notificationManagementEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public UserSetting() {
    }

    public UserSetting(User user) {
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getWeeklyWorkLogEnabled() {
        return weeklyWorkLogEnabled;
    }

    public void setWeeklyWorkLogEnabled(Boolean weeklyWorkLogEnabled) {
        this.weeklyWorkLogEnabled = weeklyWorkLogEnabled;
    }

    public Boolean getDailyWorkLogEnabled() {
        return dailyWorkLogEnabled;
    }

    public void setDailyWorkLogEnabled(Boolean dailyWorkLogEnabled) {
        this.dailyWorkLogEnabled = dailyWorkLogEnabled;
    }

    public Boolean getFeedbackReviewEnabled() {
        return feedbackReviewEnabled;
    }

    public void setFeedbackReviewEnabled(Boolean feedbackReviewEnabled) {
        this.feedbackReviewEnabled = feedbackReviewEnabled;
    }

    public Boolean getSkillGrowthTrackingEnabled() {
        return skillGrowthTrackingEnabled;
    }

    public void setSkillGrowthTrackingEnabled(Boolean skillGrowthTrackingEnabled) {
        this.skillGrowthTrackingEnabled = skillGrowthTrackingEnabled;
    }

    public Boolean getNotificationManagementEnabled() {
        return notificationManagementEnabled;
    }

    public void setNotificationManagementEnabled(Boolean notificationManagementEnabled) {
        this.notificationManagementEnabled = notificationManagementEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}