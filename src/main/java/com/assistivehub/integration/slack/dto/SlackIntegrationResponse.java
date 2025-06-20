package com.assistivehub.integration.slack.dto;

import com.assistivehub.entity.SlackIntegration;
import com.assistivehub.integration.slack.dto.SlackManualSetupRequest;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class SlackIntegrationResponse {

    private Long id;
    private Long serviceId;
    private String teamId;
    private String teamName;
    private String slackUserId;
    private String slackUserName;
    private String serviceName;
    private Boolean isActive;
    private Boolean isBot;
    private String scopes;

    // 연동 설정 정보 (복호화된 상태로 반환)
    private List<SlackManualSetupRequest.ChannelInfo> monitoringChannels;
    private SlackManualSetupRequest.NotificationSettings notificationSettings;
    private List<String> keywords;
    private Boolean enableMentions;
    private Boolean enableDirectMessages;
    private Boolean enableChannelMessages;
    private Boolean enableThreadReplies;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime connectedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime installedAt;

    // 생성자
    public SlackIntegrationResponse() {
    }

    public SlackIntegrationResponse(SlackIntegration slackIntegration) {
        this.id = slackIntegration.getId();
        this.serviceId = slackIntegration.getIntegratedService().getId();
        this.teamId = slackIntegration.getTeamId();
        this.teamName = slackIntegration.getTeamName();
        this.slackUserId = slackIntegration.getSlackUserId();
        this.slackUserName = slackIntegration.getSlackUserName();
        this.serviceName = slackIntegration.getIntegratedService().getServiceName();
        this.isActive = slackIntegration.getIntegratedService().getIsActive();
        this.isBot = slackIntegration.getIsBot();
        this.scopes = slackIntegration.getScopes();
        this.connectedAt = slackIntegration.getIntegratedService().getConnectedAt();
        this.lastSyncAt = slackIntegration.getIntegratedService().getLastSyncAt();
        this.installedAt = slackIntegration.getInstalledAt();

        // 개별 알림 설정 (DB에서 직접 가져옴)
        this.enableMentions = slackIntegration.getEnableMentions();
        this.enableDirectMessages = slackIntegration.getEnableDirectMessages();
        this.enableChannelMessages = slackIntegration.getEnableChannelMessages();
        this.enableThreadReplies = slackIntegration.getEnableThreadReplies();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getSlackUserId() {
        return slackUserId;
    }

    public void setSlackUserId(String slackUserId) {
        this.slackUserId = slackUserId;
    }

    public String getSlackUserName() {
        return slackUserName;
    }

    public void setSlackUserName(String slackUserName) {
        this.slackUserName = slackUserName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsBot() {
        return isBot;
    }

    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public List<SlackManualSetupRequest.ChannelInfo> getMonitoringChannels() {
        return monitoringChannels;
    }

    public void setMonitoringChannels(List<SlackManualSetupRequest.ChannelInfo> monitoringChannels) {
        this.monitoringChannels = monitoringChannels;
    }

    public SlackManualSetupRequest.NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(SlackManualSetupRequest.NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Boolean getEnableMentions() {
        return enableMentions;
    }

    public void setEnableMentions(Boolean enableMentions) {
        this.enableMentions = enableMentions;
    }

    public Boolean getEnableDirectMessages() {
        return enableDirectMessages;
    }

    public void setEnableDirectMessages(Boolean enableDirectMessages) {
        this.enableDirectMessages = enableDirectMessages;
    }

    public Boolean getEnableChannelMessages() {
        return enableChannelMessages;
    }

    public void setEnableChannelMessages(Boolean enableChannelMessages) {
        this.enableChannelMessages = enableChannelMessages;
    }

    public Boolean getEnableThreadReplies() {
        return enableThreadReplies;
    }

    public void setEnableThreadReplies(Boolean enableThreadReplies) {
        this.enableThreadReplies = enableThreadReplies;
    }
}