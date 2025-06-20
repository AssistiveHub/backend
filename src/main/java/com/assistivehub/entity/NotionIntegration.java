package com.assistivehub.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notion_integrations")
public class NotionIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "integrated_service_id", referencedColumnName = "id")
    private IntegratedService integratedService;

    @Column(name = "workspace_name", nullable = false)
    private String workspaceName;

    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;

    @Column(name = "bot_id")
    private String botId;

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "workspace_icon")
    private String workspaceIcon;

    @Column(name = "owner_user_id")
    private String ownerUserId;

    @Column(name = "duplicated_template_id")
    private String duplicatedTemplateId;

    // 암호화된 설정 정보들
    @Column(name = "encrypted_databases", columnDefinition = "TEXT")
    private String encryptedDatabases;

    @Column(name = "encrypted_pages", columnDefinition = "TEXT")
    private String encryptedPages;

    @Column(name = "encrypted_templates", columnDefinition = "TEXT")
    private String encryptedTemplates;

    @Column(name = "encrypted_sync_settings", columnDefinition = "TEXT")
    private String encryptedSyncSettings;

    // 개별 설정들
    @Column(name = "auto_sync_enabled")
    private Boolean autoSyncEnabled = true;

    @Column(name = "sync_interval_minutes")
    private Integer syncIntervalMinutes = 60;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;

    @Column(name = "template_auto_apply")
    private Boolean templateAutoApply = false;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "created_at", nullable = false)
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

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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

    public String getEncryptedDatabases() {
        return encryptedDatabases;
    }

    public void setEncryptedDatabases(String encryptedDatabases) {
        this.encryptedDatabases = encryptedDatabases;
    }

    public String getEncryptedPages() {
        return encryptedPages;
    }

    public void setEncryptedPages(String encryptedPages) {
        this.encryptedPages = encryptedPages;
    }

    public String getEncryptedTemplates() {
        return encryptedTemplates;
    }

    public void setEncryptedTemplates(String encryptedTemplates) {
        this.encryptedTemplates = encryptedTemplates;
    }

    public String getEncryptedSyncSettings() {
        return encryptedSyncSettings;
    }

    public void setEncryptedSyncSettings(String encryptedSyncSettings) {
        this.encryptedSyncSettings = encryptedSyncSettings;
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