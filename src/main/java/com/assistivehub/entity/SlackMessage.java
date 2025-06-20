package com.assistivehub.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slack_messages")
public class SlackMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private SlackIntegration slackIntegration;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "slack_user_id", nullable = false)
    private String slackUserId;

    @Column(name = "slack_user_name")
    private String slackUserName;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "is_mention", nullable = false)
    private Boolean isMention = false;

    @Column(name = "is_direct_message", nullable = false)
    private Boolean isDirectMessage = false;

    @Column(name = "is_thread", nullable = false)
    private Boolean isThread = false;

    @Column(name = "thread_ts")
    private String threadTs;

    @Column(name = "parent_message_id")
    private String parentMessageId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageType {
        MESSAGE("일반 메시지"),
        MENTION("멘션"),
        DIRECT_MESSAGE("다이렉트 메시지"),
        THREAD_REPLY("스레드 답글"),
        FILE_SHARE("파일 공유"),
        REACTION("리액션"),
        SYSTEM("시스템 메시지");

        private final String displayName;

        MessageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public SlackMessage() {
    }

    public SlackMessage(SlackIntegration slackIntegration, String messageId, String channelId) {
        this.slackIntegration = slackIntegration;
        this.messageId = messageId;
        this.channelId = channelId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SlackIntegration getSlackIntegration() {
        return slackIntegration;
    }

    public void setSlackIntegration(SlackIntegration slackIntegration) {
        this.slackIntegration = slackIntegration;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Boolean getIsMention() {
        return isMention;
    }

    public void setIsMention(Boolean isMention) {
        this.isMention = isMention;
    }

    public Boolean getIsDirectMessage() {
        return isDirectMessage;
    }

    public void setIsDirectMessage(Boolean isDirectMessage) {
        this.isDirectMessage = isDirectMessage;
    }

    public Boolean getIsThread() {
        return isThread;
    }

    public void setIsThread(Boolean isThread) {
        this.isThread = isThread;
    }

    public String getThreadTs() {
        return threadTs;
    }

    public void setThreadTs(String threadTs) {
        this.threadTs = threadTs;
    }

    public String getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}