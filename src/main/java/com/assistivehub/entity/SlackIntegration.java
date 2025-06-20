package com.assistivehub.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slack_integrations")
public class SlackIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private IntegratedService integratedService;

    @Column(name = "team_id", nullable = false)
    private String teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "slack_user_id", nullable = false)
    private String slackUserId;

    @Column(name = "slack_user_name")
    private String slackUserName;

    @Column(name = "bot_token", columnDefinition = "TEXT")
    private String botToken;

    @Column(name = "user_token", columnDefinition = "TEXT")
    private String userToken;

    @Column(name = "webhook_url", columnDefinition = "TEXT")
    private String webhookUrl;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "is_bot", nullable = false)
    private Boolean isBot = false;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "enterprise_id")
    private String enterpriseId;

    @Column(name = "monitoring_channels", columnDefinition = "TEXT")
    private String monitoringChannels; // JSON 형태로 암호화 저장

    @Column(name = "notification_settings", columnDefinition = "TEXT")
    private String notificationSettings; // JSON 형태로 암호화 저장

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // 키워드 목록을 암호화 저장

    @Column(name = "enable_mentions", nullable = false)
    private Boolean enableMentions = true;

    @Column(name = "enable_direct_messages", nullable = false)
    private Boolean enableDirectMessages = true;

    @Column(name = "enable_channel_messages", nullable = false)
    private Boolean enableChannelMessages = false;

    @Column(name = "enable_thread_replies", nullable = false)
    private Boolean enableThreadReplies = true;

    @Column(name = "installed_at", nullable = false)
    private LocalDateTime installedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (installedAt == null) {
            installedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public SlackIntegration() {
    }

    public SlackIntegration(IntegratedService integratedService, String teamId, String slackUserId) {
        this.integratedService = integratedService;
        this.teamId = teamId;
        this.slackUserId = slackUserId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IntegratedService getIntegratedService() {
        return integratedService;
    }

    public void setIntegratedService(IntegratedService integratedService) {
        this.integratedService = integratedService;
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

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public Boolean getIsBot() {
        return isBot;
    }

    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
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

    public String getMonitoringChannels() {
        return monitoringChannels;
    }

    public void setMonitoringChannels(String monitoringChannels) {
        this.monitoringChannels = monitoringChannels;
    }

    public String getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(String notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
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