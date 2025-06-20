package com.assistivehub.integration.notion.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;
import java.util.List;

public class NotionManualSetupRequest {

    @NotBlank(message = "워크스페이스 이름은 필수입니다.")
    private String workspaceName;

    @NotBlank(message = "액세스 토큰은 필수입니다.")
    private String accessToken;

    private String botId;
    private String workspaceId;
    private String workspaceIcon;
    private String ownerUserId;
    private String duplicatedTemplateId;

    @Valid
    private List<DatabaseInfo> databases;

    @Valid
    private List<PageInfo> pages;

    @Valid
    private List<TemplateInfo> templates;

    @Valid
    private SyncSettings syncSettings;

    private Boolean autoSyncEnabled = true;
    private Integer syncIntervalMinutes = 60;
    private Boolean notificationEnabled = true;
    private Boolean templateAutoApply = false;

    // 데이터베이스 정보
    public static class DatabaseInfo {
        @NotBlank(message = "데이터베이스 ID는 필수입니다.")
        private String databaseId;

        @NotBlank(message = "데이터베이스 이름은 필수입니다.")
        private String name;

        private String description;
        private String url;
        private Boolean syncEnabled = true;
        private String syncType; // "read", "write", "read_write"

        // Getters and Setters
        public String getDatabaseId() {
            return databaseId;
        }

        public void setDatabaseId(String databaseId) {
            this.databaseId = databaseId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public Boolean getSyncEnabled() {
            return syncEnabled;
        }

        public void setSyncEnabled(Boolean syncEnabled) {
            this.syncEnabled = syncEnabled;
        }

        public String getSyncType() {
            return syncType;
        }

        public void setSyncType(String syncType) {
            this.syncType = syncType;
        }
    }

    // 페이지 정보
    public static class PageInfo {
        @NotBlank(message = "페이지 ID는 필수입니다.")
        private String pageId;

        @NotBlank(message = "페이지 제목은 필수입니다.")
        private String title;

        private String parentId;
        private String url;
        private Boolean syncEnabled = true;
        private String pageType; // "page", "database"

        // Getters and Setters
        public String getPageId() {
            return pageId;
        }

        public void setPageId(String pageId) {
            this.pageId = pageId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Boolean getSyncEnabled() {
            return syncEnabled;
        }

        public void setSyncEnabled(Boolean syncEnabled) {
            this.syncEnabled = syncEnabled;
        }

        public String getPageType() {
            return pageType;
        }

        public void setPageType(String pageType) {
            this.pageType = pageType;
        }
    }

    // 템플릿 정보
    public static class TemplateInfo {
        @NotBlank(message = "템플릿 ID는 필수입니다.")
        private String templateId;

        @NotBlank(message = "템플릿 이름은 필수입니다.")
        private String name;

        private String description;
        private String templateType; // "page", "database"
        private Boolean autoApply = false;

        // Getters and Setters
        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTemplateType() {
            return templateType;
        }

        public void setTemplateType(String templateType) {
            this.templateType = templateType;
        }

        public Boolean getAutoApply() {
            return autoApply;
        }

        public void setAutoApply(Boolean autoApply) {
            this.autoApply = autoApply;
        }
    }

    // 동기화 설정
    public static class SyncSettings {
        private Boolean syncOnCreate = true;
        private Boolean syncOnUpdate = true;
        private Boolean syncOnDelete = false;
        private Boolean bidirectionalSync = false;
        private List<String> excludedProperties;

        // Getters and Setters
        public Boolean getSyncOnCreate() {
            return syncOnCreate;
        }

        public void setSyncOnCreate(Boolean syncOnCreate) {
            this.syncOnCreate = syncOnCreate;
        }

        public Boolean getSyncOnUpdate() {
            return syncOnUpdate;
        }

        public void setSyncOnUpdate(Boolean syncOnUpdate) {
            this.syncOnUpdate = syncOnUpdate;
        }

        public Boolean getSyncOnDelete() {
            return syncOnDelete;
        }

        public void setSyncOnDelete(Boolean syncOnDelete) {
            this.syncOnDelete = syncOnDelete;
        }

        public Boolean getBidirectionalSync() {
            return bidirectionalSync;
        }

        public void setBidirectionalSync(Boolean bidirectionalSync) {
            this.bidirectionalSync = bidirectionalSync;
        }

        public List<String> getExcludedProperties() {
            return excludedProperties;
        }

        public void setExcludedProperties(List<String> excludedProperties) {
            this.excludedProperties = excludedProperties;
        }
    }

    // Main class Getters and Setters
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

    public List<DatabaseInfo> getDatabases() {
        return databases;
    }

    public void setDatabases(List<DatabaseInfo> databases) {
        this.databases = databases;
    }

    public List<PageInfo> getPages() {
        return pages;
    }

    public void setPages(List<PageInfo> pages) {
        this.pages = pages;
    }

    public List<TemplateInfo> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateInfo> templates) {
        this.templates = templates;
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
}