package com.assistivehub.integration.notion.dto;

import com.assistivehub.entity.NotionIntegration;
import com.assistivehub.integration.notion.dto.NotionManualSetupRequest;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class NotionIntegrationResponse {

    private Long id;
    private Long integrationId;
    private String serviceName;
    private String workspaceName;
    private String maskedAccessToken;
    private String botId;
    private String workspaceId;
    private String workspaceIcon;
    private String ownerUserId;
    private String duplicatedTemplateId;
    private Boolean isActive;

    // 복호화된 설정 정보들
    private List<NotionManualSetupRequest.DatabaseInfo> databases;
    private List<NotionManualSetupRequest.PageInfo> pages;
    private List<NotionManualSetupRequest.TemplateInfo> templates;
    private NotionManualSetupRequest.SyncSettings syncSettings;

    // 개별 설정들
    private Boolean autoSyncEnabled;
    private Integer syncIntervalMinutes;
    private Boolean notificationEnabled;
    private Boolean templateAutoApply;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public NotionIntegrationResponse() {
    }

    // 기본 정보만 있는 생성자 (암호화된 정보 없이)
    public NotionIntegrationResponse(NotionIntegration integration) {
        this.id = integration.getId();
        this.integrationId = integration.getIntegratedService().getId();
        this.serviceName = integration.getIntegratedService().getServiceName();
        this.workspaceName = integration.getWorkspaceName();
        this.maskedAccessToken = maskAccessToken(integration.getAccessToken());
        this.botId = integration.getBotId();
        this.workspaceId = integration.getWorkspaceId();
        this.workspaceIcon = integration.getWorkspaceIcon();
        this.ownerUserId = integration.getOwnerUserId();
        this.duplicatedTemplateId = integration.getDuplicatedTemplateId();
        this.isActive = integration.getIntegratedService().getIsActive();
        this.autoSyncEnabled = integration.getAutoSyncEnabled();
        this.syncIntervalMinutes = integration.getSyncIntervalMinutes();
        this.notificationEnabled = integration.getNotificationEnabled();
        this.templateAutoApply = integration.getTemplateAutoApply();
        this.lastSyncAt = integration.getLastSyncAt();
        this.createdAt = integration.getCreatedAt();
        this.updatedAt = integration.getUpdatedAt();
    }

    /**
     * 액세스 토큰 마스킹 처리
     */
    private String maskAccessToken(String token) {
        if (token == null || token.length() < 10) {
            return "****";
        }

        // 노션 토큰 형식: secret_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        if (token.startsWith("secret_")) {
            String secretPart = token.substring(7); // "secret_" 제거
            if (secretPart.length() > 12) {
                return "secret_" + secretPart.substring(0, 8) + "****" + secretPart.substring(secretPart.length() - 4);
            }
        }

        // 일반적인 토큰 마스킹
        if (token.length() > 12) {
            return token.substring(0, 8) + "****" + token.substring(token.length() - 4);
        }

        return token.substring(0, 4) + "****";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(Long integrationId) {
        this.integrationId = integrationId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getMaskedAccessToken() {
        return maskedAccessToken;
    }

    public void setMaskedAccessToken(String maskedAccessToken) {
        this.maskedAccessToken = maskedAccessToken;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceIcon() {
        return workspaceIcon;
    }

    public void setWorkspaceIcon(String workspaceIcon) {
        this.workspaceIcon = workspaceIcon;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getDuplicatedTemplateId() {
        return duplicatedTemplateId;
    }

    public void setDuplicatedTemplateId(String duplicatedTemplateId) {
        this.duplicatedTemplateId = duplicatedTemplateId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<NotionManualSetupRequest.DatabaseInfo> getDatabases() {
        return databases;
    }

    public void setDatabases(List<NotionManualSetupRequest.DatabaseInfo> databases) {
        this.databases = databases;
    }

    public List<NotionManualSetupRequest.PageInfo> getPages() {
        return pages;
    }

    public void setPages(List<NotionManualSetupRequest.PageInfo> pages) {
        this.pages = pages;
    }

    public List<NotionManualSetupRequest.TemplateInfo> getTemplates() {
        return templates;
    }

    public void setTemplates(List<NotionManualSetupRequest.TemplateInfo> templates) {
        this.templates = templates;
    }

    public NotionManualSetupRequest.SyncSettings getSyncSettings() {
        return syncSettings;
    }

    public void setSyncSettings(NotionManualSetupRequest.SyncSettings syncSettings) {
        this.syncSettings = syncSettings;
    }

    public Boolean getAutoSyncEnabled() {
        return autoSyncEnabled;
    }

    public void setAutoSyncEnabled(Boolean autoSyncEnabled) {
        this.autoSyncEnabled = autoSyncEnabled;
    }

    public Integer getSyncIntervalMinutes() {
        return syncIntervalMinutes;
    }

    public void setSyncIntervalMinutes(Integer syncIntervalMinutes) {
        this.syncIntervalMinutes = syncIntervalMinutes;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Boolean getTemplateAutoApply() {
        return templateAutoApply;
    }

    public void setTemplateAutoApply(Boolean templateAutoApply) {
        this.templateAutoApply = templateAutoApply;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
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