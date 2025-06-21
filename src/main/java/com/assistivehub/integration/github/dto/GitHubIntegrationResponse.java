package com.assistivehub.integration.github.dto;

import com.assistivehub.entity.GitHubIntegration;
import com.assistivehub.util.EncryptionUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitHubIntegrationResponse {

    private Long id;
    private String username;
    private String maskedAccessToken;
    private String githubUserId;
    private String avatarUrl;
    private String profileUrl;
    private String email;
    private String serviceName;
    private String workspaceName;
    private Boolean isActive;

    // 복호화된 설정들
    private List<RepositoryInfo> repositories;
    private List<WebhookInfo> webhooks;
    private NotificationSettings notificationSettings;
    private SyncSettings syncSettings;

    // 개별 설정들
    private Boolean autoSyncEnabled;
    private Boolean syncCommits;
    private Boolean syncPullRequests;
    private Boolean syncIssues;
    private Boolean syncReleases;
    private Boolean notificationEnabled;
    private Boolean webhookEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime connectedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 중첩 클래스들
    public static class RepositoryInfo {
        private String name;
        private String fullName;
        private String url;
        private Boolean isPrivate;
        private String defaultBranch;
        private String description;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Boolean getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(Boolean isPrivate) {
            this.isPrivate = isPrivate;
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class WebhookInfo {
        private String url;
        private List<String> events;
        private Boolean isActive;
        private String secret;

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getEvents() {
            return events;
        }

        public void setEvents(List<String> events) {
            this.events = events;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class NotificationSettings {
        private Boolean pushNotifications;
        private Boolean pullRequestNotifications;
        private Boolean issueNotifications;
        private Boolean releaseNotifications;
        private List<String> emailRecipients;

        // Getters and Setters
        public Boolean getPushNotifications() {
            return pushNotifications;
        }

        public void setPushNotifications(Boolean pushNotifications) {
            this.pushNotifications = pushNotifications;
        }

        public Boolean getPullRequestNotifications() {
            return pullRequestNotifications;
        }

        public void setPullRequestNotifications(Boolean pullRequestNotifications) {
            this.pullRequestNotifications = pullRequestNotifications;
        }

        public Boolean getIssueNotifications() {
            return issueNotifications;
        }

        public void setIssueNotifications(Boolean issueNotifications) {
            this.issueNotifications = issueNotifications;
        }

        public Boolean getReleaseNotifications() {
            return releaseNotifications;
        }

        public void setReleaseNotifications(Boolean releaseNotifications) {
            this.releaseNotifications = releaseNotifications;
        }

        public List<String> getEmailRecipients() {
            return emailRecipients;
        }

        public void setEmailRecipients(List<String> emailRecipients) {
            this.emailRecipients = emailRecipients;
        }
    }

    public static class SyncSettings {
        private Integer syncIntervalMinutes;
        private Boolean bidirectionalSync;
        private List<String> ignoredBranches;
        private List<String> watchedBranches;

        // Getters and Setters
        public Integer getSyncIntervalMinutes() {
            return syncIntervalMinutes;
        }

        public void setSyncIntervalMinutes(Integer syncIntervalMinutes) {
            this.syncIntervalMinutes = syncIntervalMinutes;
        }

        public Boolean getBidirectionalSync() {
            return bidirectionalSync;
        }

        public void setBidirectionalSync(Boolean bidirectionalSync) {
            this.bidirectionalSync = bidirectionalSync;
        }

        public List<String> getIgnoredBranches() {
            return ignoredBranches;
        }

        public void setIgnoredBranches(List<String> ignoredBranches) {
            this.ignoredBranches = ignoredBranches;
        }

        public List<String> getWatchedBranches() {
            return watchedBranches;
        }

        public void setWatchedBranches(List<String> watchedBranches) {
            this.watchedBranches = watchedBranches;
        }
    }

    // 정적 팩토리 메서드
    public static GitHubIntegrationResponse fromEntity(GitHubIntegration integration) {
        GitHubIntegrationResponse response = new GitHubIntegrationResponse();

        response.setId(integration.getId());
        response.setUsername(integration.getUsername());
        response.setMaskedAccessToken(maskAccessToken(integration.getAccessToken()));
        response.setGithubUserId(integration.getGithubUserId());
        response.setAvatarUrl(integration.getAvatarUrl());
        response.setProfileUrl(integration.getProfileUrl());
        response.setEmail(integration.getEmail());

        // IntegratedService 정보
        if (integration.getIntegratedService() != null) {
            response.setServiceName(integration.getIntegratedService().getServiceName());
            response.setWorkspaceName(integration.getIntegratedService().getWorkspaceName());
            response.setIsActive(integration.getIntegratedService().getIsActive());
            response.setConnectedAt(integration.getIntegratedService().getConnectedAt());
        }

        // 개별 설정들
        response.setAutoSyncEnabled(integration.getAutoSyncEnabled());
        response.setSyncCommits(integration.getSyncCommits());
        response.setSyncPullRequests(integration.getSyncPullRequests());
        response.setSyncIssues(integration.getSyncIssues());
        response.setSyncReleases(integration.getSyncReleases());
        response.setNotificationEnabled(integration.getNotificationEnabled());
        response.setWebhookEnabled(integration.getWebhookEnabled());
        response.setLastSyncAt(integration.getLastSyncAt());
        response.setCreatedAt(integration.getCreatedAt());

        // 암호화된 설정들 복호화
        response.setRepositories(decryptRepositories(integration.getEncryptedRepositories()));
        response.setWebhooks(decryptWebhooks(integration.getEncryptedWebhooks()));
        response.setNotificationSettings(decryptNotificationSettings(integration.getEncryptedNotificationSettings()));
        response.setSyncSettings(decryptSyncSettings(integration.getEncryptedSyncSettings()));

        return response;
    }

    // 토큰 마스킹
    private static String maskAccessToken(String token) {
        if (token == null || token.length() < 12) {
            return "****";
        }

        if (token.startsWith("ghp_")) {
            // GitHub Personal Access Token 형식
            return token.substring(0, 8) + "****" + token.substring(token.length() - 4);
        } else if (token.startsWith("gho_")) {
            // GitHub OAuth Token 형식
            return token.substring(0, 8) + "****" + token.substring(token.length() - 4);
        } else {
            // 일반적인 토큰
            return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
        }
    }

    // 복호화 헬퍼 메서드들
    private static List<RepositoryInfo> decryptRepositories(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return null;
        }

        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil();
            String decrypted = encryptionUtil.decrypt(encrypted);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(decrypted, new TypeReference<List<RepositoryInfo>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private static List<WebhookInfo> decryptWebhooks(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return null;
        }

        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil();
            String decrypted = encryptionUtil.decrypt(encrypted);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(decrypted, new TypeReference<List<WebhookInfo>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private static NotificationSettings decryptNotificationSettings(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return null;
        }

        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil();
            String decrypted = encryptionUtil.decrypt(encrypted);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(decrypted, NotificationSettings.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static SyncSettings decryptSyncSettings(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return null;
        }

        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil();
            String decrypted = encryptionUtil.decrypt(encrypted);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(decrypted, SyncSettings.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMaskedAccessToken() {
        return maskedAccessToken;
    }

    public void setMaskedAccessToken(String maskedAccessToken) {
        this.maskedAccessToken = maskedAccessToken;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<RepositoryInfo> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryInfo> repositories) {
        this.repositories = repositories;
    }

    public List<WebhookInfo> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(List<WebhookInfo> webhooks) {
        this.webhooks = webhooks;
    }

    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public SyncSettings getSyncSettings() {
        return syncSettings;
    }

    public void setSyncSettings(SyncSettings syncSettings) {
        this.syncSettings = syncSettings;
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

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}