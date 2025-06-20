package com.assistivehub.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "github_integrations")
public class GitHubIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "integrated_service_id", referencedColumnName = "id")
    private IntegratedService integratedService;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;

    @Column(name = "github_user_id")
    private String githubUserId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "email")
    private String email;

    // 암호화된 설정 정보들
    @Column(name = "encrypted_repositories", columnDefinition = "TEXT")
    private String encryptedRepositories;

    @Column(name = "encrypted_webhooks", columnDefinition = "TEXT")
    private String encryptedWebhooks;

    @Column(name = "encrypted_notification_settings", columnDefinition = "TEXT")
    private String encryptedNotificationSettings;

    @Column(name = "encrypted_sync_settings", columnDefinition = "TEXT")
    private String encryptedSyncSettings;

    // 개별 설정들
    @Column(name = "auto_sync_enabled")
    private Boolean autoSyncEnabled = true;

    @Column(name = "sync_commits")
    private Boolean syncCommits = true;

    @Column(name = "sync_pull_requests")
    private Boolean syncPullRequests = true;

    @Column(name = "sync_issues")
    private Boolean syncIssues = true;

    @Column(name = "sync_releases")
    private Boolean syncReleases = false;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;

    @Column(name = "webhook_enabled")
    private Boolean webhookEnabled = false;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getGithubUserId() {
        return githubUserId;
    }

    public void setGithubUserId(String githubUserId) {
        this.githubUserId = githubUserId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncryptedRepositories() {
        return encryptedRepositories;
    }

    public void setEncryptedRepositories(String encryptedRepositories) {
        this.encryptedRepositories = encryptedRepositories;
    }

    public String getEncryptedWebhooks() {
        return encryptedWebhooks;
    }

    public void setEncryptedWebhooks(String encryptedWebhooks) {
        this.encryptedWebhooks = encryptedWebhooks;
    }

    public String getEncryptedNotificationSettings() {
        return encryptedNotificationSettings;
    }

    public void setEncryptedNotificationSettings(String encryptedNotificationSettings) {
        this.encryptedNotificationSettings = encryptedNotificationSettings;
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

    public Boolean getSyncCommits() {
        return syncCommits;
    }

    public void setSyncCommits(Boolean syncCommits) {
        this.syncCommits = syncCommits;
    }

    public Boolean getSyncPullRequests() {
        return syncPullRequests;
    }

    public void setSyncPullRequests(Boolean syncPullRequests) {
        this.syncPullRequests = syncPullRequests;
    }

    public Boolean getSyncIssues() {
        return syncIssues;
    }

    public void setSyncIssues(Boolean syncIssues) {
        this.syncIssues = syncIssues;
    }

    public Boolean getSyncReleases() {
        return syncReleases;
    }

    public void setSyncReleases(Boolean syncReleases) {
        this.syncReleases = syncReleases;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Boolean getWebhookEnabled() {
        return webhookEnabled;
    }

    public void setWebhookEnabled(Boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
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