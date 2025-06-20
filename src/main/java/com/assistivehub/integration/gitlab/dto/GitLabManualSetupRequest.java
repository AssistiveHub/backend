package com.assistivehub.integration.gitlab.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.Valid;
import java.util.List;

public class GitLabManualSetupRequest {

    @NotBlank(message = "사용자명은 필수입니다.")
    private String username;

    @NotBlank(message = "액세스 토큰은 필수입니다.")
    private String accessToken;

    private String gitlabUrl = "https://gitlab.com"; // 기본값
    private String gitlabUserId;
    private String avatarUrl;
    private String profileUrl;
    private String email;

    @Valid
    private List<ProjectInfo> projects;

    @Valid
    private List<WebhookInfo> webhooks;

    @Valid
    private NotificationSettings notificationSettings;

    @Valid
    private SyncSettings syncSettings;

    private Boolean autoSyncEnabled = true;
    private Boolean syncCommits = true;
    private Boolean syncMergeRequests = true;
    private Boolean syncIssues = true;
    private Boolean syncReleases = false;
    private Boolean syncPipelines = false;
    private Boolean notificationEnabled = true;
    private Boolean webhookEnabled = false;

    // 프로젝트 정보
    public static class ProjectInfo {
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        private String name;

        private String fullName;
        private String description;
        private String url;
        private Boolean isPrivate = false;
        private Boolean syncEnabled = true;
        private String defaultBranch = "main";
        private String namespace;

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

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }

    // 웹훅 정보
    public static class WebhookInfo {
        @NotBlank(message = "웹훅 URL은 필수입니다.")
        private String url;

        private String token;
        private List<String> events;
        private Boolean pushEvents = true;
        private Boolean mergeRequestsEvents = true;
        private Boolean issuesEvents = true;
        private Boolean pipelineEvents = false;

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public List<String> getEvents() {
            return events;
        }

        public void setEvents(List<String> events) {
            this.events = events;
        }

        public Boolean getPushEvents() {
            return pushEvents;
        }

        public void setPushEvents(Boolean pushEvents) {
            this.pushEvents = pushEvents;
        }

        public Boolean getMergeRequestsEvents() {
            return mergeRequestsEvents;
        }

        public void setMergeRequestsEvents(Boolean mergeRequestsEvents) {
            this.mergeRequestsEvents = mergeRequestsEvents;
        }

        public Boolean getIssuesEvents() {
            return issuesEvents;
        }

        public void setIssuesEvents(Boolean issuesEvents) {
            this.issuesEvents = issuesEvents;
        }

        public Boolean getPipelineEvents() {
            return pipelineEvents;
        }

        public void setPipelineEvents(Boolean pipelineEvents) {
            this.pipelineEvents = pipelineEvents;
        }
    }

    // 알림 설정
    public static class NotificationSettings {
        private Boolean pushNotification = true;
        private Boolean mergeRequestNotification = true;
        private Boolean issueNotification = true;
        private Boolean pipelineNotification = false;
        private Boolean releaseNotification = false;

        // Getters and Setters
        public Boolean getPushNotification() {
            return pushNotification;
        }

        public void setPushNotification(Boolean pushNotification) {
            this.pushNotification = pushNotification;
        }

        public Boolean getMergeRequestNotification() {
            return mergeRequestNotification;
        }

        public void setMergeRequestNotification(Boolean mergeRequestNotification) {
            this.mergeRequestNotification = mergeRequestNotification;
        }

        public Boolean getIssueNotification() {
            return issueNotification;
        }

        public void setIssueNotification(Boolean issueNotification) {
            this.issueNotification = issueNotification;
        }

        public Boolean getPipelineNotification() {
            return pipelineNotification;
        }

        public void setPipelineNotification(Boolean pipelineNotification) {
            this.pipelineNotification = pipelineNotification;
        }

        public Boolean getReleaseNotification() {
            return releaseNotification;
        }

        public void setReleaseNotification(Boolean releaseNotification) {
            this.releaseNotification = releaseNotification;
        }
    }

    // 동기화 설정
    public static class SyncSettings {
        private Boolean syncOnPush = true;
        private Boolean syncOnMergeRequest = true;
        private Boolean syncOnIssue = true;
        private Boolean syncOnPipeline = false;
        private Boolean syncOnRelease = false;
        private Integer syncIntervalMinutes = 60;

        // Getters and Setters
        public Boolean getSyncOnPush() {
            return syncOnPush;
        }

        public void setSyncOnPush(Boolean syncOnPush) {
            this.syncOnPush = syncOnPush;
        }

        public Boolean getSyncOnMergeRequest() {
            return syncOnMergeRequest;
        }

        public void setSyncOnMergeRequest(Boolean syncOnMergeRequest) {
            this.syncOnMergeRequest = syncOnMergeRequest;
        }

        public Boolean getSyncOnIssue() {
            return syncOnIssue;
        }

        public void setSyncOnIssue(Boolean syncOnIssue) {
            this.syncOnIssue = syncOnIssue;
        }

        public Boolean getSyncOnPipeline() {
            return syncOnPipeline;
        }

        public void setSyncOnPipeline(Boolean syncOnPipeline) {
            this.syncOnPipeline = syncOnPipeline;
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

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public String getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(String gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
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

    public List<ProjectInfo> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectInfo> projects) {
        this.projects = projects;
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

    public Boolean getSyncMergeRequests() {
        return syncMergeRequests;
    }

    public void setSyncMergeRequests(Boolean syncMergeRequests) {
        this.syncMergeRequests = syncMergeRequests;
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

    public Boolean getSyncPipelines() {
        return syncPipelines;
    }

    public void setSyncPipelines(Boolean syncPipelines) {
        this.syncPipelines = syncPipelines;
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