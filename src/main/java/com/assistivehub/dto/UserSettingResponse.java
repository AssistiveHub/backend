package com.assistivehub.dto;

import com.assistivehub.entity.UserSetting;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class UserSettingResponse {

    private Long id;
    private Long userId;

    // 주간 업무일지 작성 설정
    private Boolean weeklyWorkLogEnabled;

    // 일일 업무일지 설정
    private Boolean dailyWorkLogEnabled;

    // 업무 피드백 및 리뷰 관리 설정
    private Boolean feedbackReviewEnabled;

    // 스킬 성장 측정 설정
    private Boolean skillGrowthTrackingEnabled;

    // 알람 정돈 설정
    private Boolean notificationManagementEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public UserSettingResponse() {
    }

    public UserSettingResponse(UserSetting userSetting) {
        this.id = userSetting.getId();
        this.userId = userSetting.getUser().getId();
        this.weeklyWorkLogEnabled = userSetting.getWeeklyWorkLogEnabled();
        this.dailyWorkLogEnabled = userSetting.getDailyWorkLogEnabled();
        this.feedbackReviewEnabled = userSetting.getFeedbackReviewEnabled();
        this.skillGrowthTrackingEnabled = userSetting.getSkillGrowthTrackingEnabled();
        this.notificationManagementEnabled = userSetting.getNotificationManagementEnabled();
        this.createdAt = userSetting.getCreatedAt();
        this.updatedAt = userSetting.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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