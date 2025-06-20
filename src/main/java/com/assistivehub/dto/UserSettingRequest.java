package com.assistivehub.dto;

public class UserSettingRequest {

    private Boolean weeklyWorkLogEnabled;
    private Boolean dailyWorkLogEnabled;
    private Boolean feedbackReviewEnabled;
    private Boolean skillGrowthTrackingEnabled;
    private Boolean notificationManagementEnabled;

    // Constructors
    public UserSettingRequest() {
    }

    public UserSettingRequest(Boolean weeklyWorkLogEnabled, Boolean dailyWorkLogEnabled,
            Boolean feedbackReviewEnabled, Boolean skillGrowthTrackingEnabled,
            Boolean notificationManagementEnabled) {
        this.weeklyWorkLogEnabled = weeklyWorkLogEnabled;
        this.dailyWorkLogEnabled = dailyWorkLogEnabled;
        this.feedbackReviewEnabled = feedbackReviewEnabled;
        this.skillGrowthTrackingEnabled = skillGrowthTrackingEnabled;
        this.notificationManagementEnabled = notificationManagementEnabled;
    }

    // Getters and Setters
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
}