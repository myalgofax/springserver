package com.myalgofax.service;

import org.springframework.stereotype.Service;

import com.myalgofax.dto.WebSocketMessageDto;
import com.myalgofax.websocket.AuthenticatedWebSocketHandler;

@Service
public class WebSocketService {

    private final AuthenticatedWebSocketHandler webSocketHandler;

    public WebSocketService(AuthenticatedWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public void broadcastNotification(String message) {
        WebSocketMessageDto notification = new WebSocketMessageDto(
            message, 
            "SYSTEM", 
            "System", 
            WebSocketMessageDto.MessageType.NOTIFICATION
        );
        webSocketHandler.broadcastMessage(notification);
    }

    public void broadcastSystemMessage(String message) {
        WebSocketMessageDto systemMsg = new WebSocketMessageDto(
            message, 
            "SYSTEM", 
            "System", 
            WebSocketMessageDto.MessageType.SYSTEM
        );
        webSocketHandler.broadcastMessage(systemMsg);
    }

    public int getActiveConnections() {
        return webSocketHandler.getActiveSessionCount();
    }
}