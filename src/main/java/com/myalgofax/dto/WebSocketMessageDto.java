package com.myalgofax.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WebSocketMessageDto {
    private String content;
    private String senderId;
    private String senderName;
    private MessageType type;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT, NOTIFICATION, SYSTEM, ERROR
    }

    public WebSocketMessageDto() {
        this.timestamp = LocalDateTime.now();
    }

    public WebSocketMessageDto(String content, String senderId, String senderName, MessageType type) {
        this.content = content;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}