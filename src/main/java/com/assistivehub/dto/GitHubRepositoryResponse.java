package com.assistivehub.dto;

import com.assistivehub.entity.GitHubRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitHubRepositoryResponse {

    private Long id;
    private String githubUsername;
    private String githubUserId;
    private String avatarUrl;
    private String repositoryName;
    private String repositoryFullName;
    private String repositoryUrl;
    private String repositoryDescription;
    private String repositoryLanguage;
    private Boolean isPrivate;
    private String defaultBranch;
    private Boolean isActive;

    // 통계 정보
    private Integer starsCount;
    private Integer forksCount;
    private Integer openIssuesCount;

    // 동기화 설정
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
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static GitHubRepositoryResponse fromEntity(GitHubRepository repository) {
        GitHubRepositoryResponse response = new GitHubRepositoryResponse();

        response.setId(repository.getId());
        response.setGithubUsername(repository.getGithubUsername());
        response.setGithubUserId(repository.getGithubUserId());
        response.setAvatarUrl(repository.getAvatarUrl());
        response.setRepositoryName(repository.getRepositoryName());
        response.setRepositoryFullName(repository.getRepositoryFullName());
        response.setRepositoryUrl(repository.getRepositoryUrl());
        response.setRepositoryDescription(repository.getRepositoryDescription());
        response.setRepositoryLanguage(repository.getRepositoryLanguage());
        response.setIsPrivate(repository.getIsPrivate());
        response.setDefaultBranch(repository.getDefaultBranch());
        response.setIsActive(repository.getIntegratedService().getIsActive());

        // 통계 정보
        response.setStarsCount(repository.getStarsCount());
        response.setForksCount(repository.getForksCount());
        response.setOpenIssuesCount(repository.getOpenIssuesCount());

        // 동기화 설정
        response.setAutoSyncEnabled(repository.getAutoSyncEnabled());
        response.setSyncCommits(repository.getSyncCommits());
        response.setSyncPullRequests(repository.getSyncPullRequests());
        response.setSyncIssues(repository.getSyncIssues());
        response.setSyncReleases(repository.getSyncReleases());
        response.setNotificationEnabled(repository.getNotificationEnabled());
        response.setWebhookEnabled(repository.getWebhookEnabled());

        response.setLastSyncAt(repository.getLastSyncAt());
        response.setCreatedAt(repository.getCreatedAt());

        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
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

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public void setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryDescription() {
        return repositoryDescription;
    }

    public void setRepositoryDescription(String repositoryDescription) {
        this.repositoryDescription = repositoryDescription;
    }

    public String getRepositoryLanguage() {
        return repositoryLanguage;
    }

    public void setRepositoryLanguage(String repositoryLanguage) {
        this.repositoryLanguage = repositoryLanguage;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getStarsCount() {
        return starsCount;
    }

    public void setStarsCount(Integer starsCount) {
        this.starsCount = starsCount;
    }

    public Integer getForksCount() {
        return forksCount;
    }

    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
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
}