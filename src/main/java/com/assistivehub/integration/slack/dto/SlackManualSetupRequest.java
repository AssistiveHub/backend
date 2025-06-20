package com.assistivehub.integration.slack.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class SlackManualSetupRequest {

    @NotBlank(message = "워크스페이스 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "워크스페이스 이름은 1-100자 사이여야 합니다")
    private String workspaceName;

    @NotBlank(message = "사용자 토큰은 필수입니다")
    private String userToken;

    private String botToken; // 선택사항

    private String webhookUrl; // 선택사항

    @NotBlank(message = "슬랙 사용자 ID는 필수입니다")
    private String slackUserId;

    private String slackUserName;

    @NotBlank(message = "팀 ID는 필수입니다")
    private String teamId;

    private String teamName;

    // 모니터링할 채널 목록 (선택사항)
    private List<ChannelInfo> monitoringChannels;

    // 알림 설정
    private NotificationSettings notificationSettings;

    public static class ChannelInfo {
        private String channelId;
        private String channelName;
        private Boolean isPrivate = false;
        private Boolean enableMentions = true;
        private Boolean enableDirectMessages = true;

        // 생성자
        public ChannelInfo() {
        }

        public ChannelInfo(String channelId, String channelName) {
            this.channelId = channelId;
            this.channelName = channelName;
        }

        // Getters and Setters
        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public Boolean getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(Boolean isPrivate) {
            this.isPrivate = isPrivate;
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
    }

    public static class NotificationSettings {
        private Boolean enableMentions = true;
        private Boolean enableDirectMessages = true;
        private Boolean enableChannelMessages = false;
        private Boolean enableThreadReplies = true;
        private List<String> keywords; // 키워드 알림

        // 생성자
        public NotificationSettings() {
        }

        // Getters and Setters
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

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }
    }

    // 생성자
    public SlackManualSetupRequest() {
    }

    public SlackManualSetupRequest(String workspaceName, String userToken, String slackUserId, String teamId) {
        this.workspaceName = workspaceName;
        this.userToken = userToken;
        this.slackUserId = slackUserId;
        this.teamId = teamId;
    }

    // Getters and Setters
    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
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

    public List<ChannelInfo> getMonitoringChannels() {
        return monitoringChannels;
    }

    public void setMonitoringChannels(List<ChannelInfo> monitoringChannels) {
        this.monitoringChannels = monitoringChannels;
    }

    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }
}