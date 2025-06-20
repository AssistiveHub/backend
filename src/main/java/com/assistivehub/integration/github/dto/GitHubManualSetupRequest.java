package com.assistivehub.integration.github.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.Valid;
import java.util.List;

public class GitHubManualSetupRequest {

    @NotBlank(message = "사용자명은 필수입니다.")
    private String username;

    @NotBlank(message = "액세스 토큰은 필수입니다.")
    private String accessToken;

    private String githubUserId;
    private String avatarUrl;
    private String profileUrl;
    private String email;

    @Valid
    private List<RepositoryInfo> repositories;

    @Valid
    private List<WebhookInfo> webhooks;

    @Valid
    private NotificationSettings notificationSettings;

    @Valid
    private SyncSettings syncSettings;

    private Boolean autoSyncEnabled = true;
    private Boolean syncCommits = true;
    private Boolean syncPullRequests = true;
    private Boolean syncIssues = true;
    private Boolean syncReleases = false;
    private Boolean notificationEnabled = true;
    private Boolean webhookEnabled = false;

    // 레포지토리 정보
    public static class RepositoryInfo {
        @NotBlank(message = "레포지토리 이름은 필수입니다.")
        private String name;

        private String fullName;
        private String description;
        private String url;
        private Boolean isPrivate = false;
        private Boolean syncEnabled = true;
        private String defaultBranch = "main";

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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

        public Boolean getSyncEnabled() {
            return syncEnabled;
        }

        public void setSyncEnabled(Boolean syncEnabled) {
            this.syncEnabled = syncEnabled;
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }
    }

    // 웹훅 정보
    public static class WebhookInfo {
        @NotBlank(message = "웹훅 URL은 필수입니다.")
        private String url;

        private String secret;
        private List<String> events;
        private Boolean active = true;

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public List<String> getEvents() {
            return events;
        }

        public void setEvents(List<String> events) {
            this.events = events;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }

    // 알림 설정
    public static class NotificationSettings {
        private Boolean pushNotification = true;
        private Boolean pullRequestNotification = true;
        private Boolean issueNotification = true;
        private Boolean releaseNotification = false;
        private Boolean workflowNotification = false;

        // Getters and Setters
        public Boolean getPushNotification() {
            return pushNotification;
        }

        public void setPushNotification(Boolean pushNotification) {
            this.pushNotification = pushNotification;
        }

        public Boolean getPullRequestNotification() {
            return pullRequestNotification;
        }

        public void setPullRequestNotification(Boolean pullRequestNotification) {
            this.pullRequestNotification = pullRequestNotification;
        }

        public Boolean getIssueNotification() {
            return issueNotification;
        }

        public void setIssueNotification(Boolean issueNotification) {
            this.issueNotification = issueNotification;
        }

        public Boolean getReleaseNotification() {
            return releaseNotification;
        }

        public void setReleaseNotification(Boolean releaseNotification) {
            this.releaseNotification = releaseNotification;
        }

        public Boolean getWorkflowNotification() {
            return workflowNotification;
        }

        public void setWorkflowNotification(Boolean workflowNotification) {
            this.workflowNotification = workflowNotification;
        }
    }

    // 동기화 설정
    public static class SyncSettings {
        private Boolean syncOnPush = true;
        private Boolean syncOnPullRequest = true;
        private Boolean syncOnIssue = true;
        private Boolean syncOnRelease = false;
        private Integer syncIntervalMinutes = 60;

        // Getters and Setters
        public Boolean getSyncOnPush() {
            return syncOnPush;
        }

        public void setSyncOnPush(Boolean syncOnPush) {
            this.syncOnPush = syncOnPush;
        }

        public Boolean getSyncOnPullRequest() {
            return syncOnPullRequest;
        }

        public void setSyncOnPullRequest(Boolean syncOnPullRequest) {
            this.syncOnPullRequest = syncOnPullRequest;
        }

        public Boolean getSyncOnIssue() {
            return syncOnIssue;
        }

        public void setSyncOnIssue(Boolean syncOnIssue) {
            this.syncOnIssue = syncOnIssue;
        }

        public Boolean getSyncOnRelease() {
            return syncOnRelease;
        }

        public void setSyncOnRelease(Boolean syncOnRelease) {
            this.syncOnRelease = syncOnRelease;
        }

        public Integer getSyncIntervalMinutes() {
            return syncIntervalMinutes;
        }

        public void setSyncIntervalMinutes(Integer syncIntervalMinutes) {
            this.syncIntervalMinutes = syncIntervalMinutes;
        }
    }

    // Main class Getters and Setters
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
}