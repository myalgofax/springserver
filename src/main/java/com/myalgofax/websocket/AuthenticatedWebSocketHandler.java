package com.myalgofax.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myalgofax.dto.WebSocketAuthDto;
import com.myalgofax.dto.WebSocketMessageDto;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.user.entity.User;
import com.myalgofax.repository.UserRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class AuthenticatedWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatedWebSocketHandler.class);
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> authenticatedSessions = new ConcurrentHashMap<>();
    private final Sinks.Many<WebSocketMessageDto> messageSink = Sinks.many().multicast().onBackpressureBuffer();

    public AuthenticatedWebSocketHandler(JwtUtil jwtUtil, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
            .cast(WebSocketMessage.class)
            .take(1) // First message should be authentication
            .flatMap(message -> authenticateSession(session, message))
            .then(handleAuthenticatedSession(session))
            .doOnTerminate(() -> removeSession(session))
            .onErrorResume(error -> {
                logger.error("WebSocket error for session {}: {}", session.getId(), error.getMessage());
                return sendErrorAndClose(session, "Connection error: " + error.getMessage());
            });
    }

    private Mono<Void> authenticateSession(WebSocketSession session, WebSocketMessage message) {
        try {
            String payload = message.getPayloadAsText();
            WebSocketAuthDto authDto = objectMapper.readValue(payload, WebSocketAuthDto.class);
            
            if (authDto.getUserToken() == null || authDto.getUserToken().trim().isEmpty()) {
                return sendErrorAndClose(session, "Missing userToken in connection payload");
            }

            String token = authDto.getUserToken().trim();
            if (!jwtUtil.validateToken(token)) {
                return sendErrorAndClose(session, "Invalid or expired token");
            }

            String userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return sendErrorAndClose(session, "Invalid token: missing user information");
            }

            return userRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.error(new RuntimeException("User account is inactive"));
                    }
                    
                    // Store authenticated session
                    session.getAttributes().put("userId", user.getUserId());
                    session.getAttributes().put("userEmail", user.getEmail());
                    session.getAttributes().put("userName", user.getFirstName() + " " + user.getLastName());
                    authenticatedSessions.put(session.getId(), session);
                    
                    logger.info("WebSocket authenticated for user: {}", user.getUserId());
                    
                    // Send welcome message
                    WebSocketMessageDto welcomeMsg = new WebSocketMessageDto(
                        "Connected successfully", 
                        "SYSTEM", 
                        "System", 
                        WebSocketMessageDto.MessageType.SYSTEM
                    );
                    
                    return sendMessage(session, welcomeMsg);
                })
                .onErrorResume(error -> sendErrorAndClose(session, "Authentication failed: " + error.getMessage()));

        } catch (Exception e) {
            logger.error("Authentication parsing error: {}", e.getMessage());
            return sendErrorAndClose(session, "Invalid authentication payload format");
        }
    }

    private Mono<Void> handleAuthenticatedSession(WebSocketSession session) {
        // Listen for incoming messages and broadcast them
        Mono<Void> input = session.receive()
            .skip(1) // Skip the auth message
            .flatMap(message -> processIncomingMessage(session, message))
            .then();

        // Send outgoing messages to this session
        Flux<WebSocketMessage> output = messageSink.asFlux()
            .map(msg -> {
                try {
                    return session.textMessage(objectMapper.writeValueAsString(msg));
                } catch (Exception e) {
                    logger.error("Error serializing message: {}", e.getMessage());
                    return session.textMessage("{\"error\":\"Message serialization failed\"}");
                }
            });

        return session.send(output).and(input);
    }

    private Mono<Void> processIncomingMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            String userId = (String) session.getAttributes().get("userId");
            String userName = (String) session.getAttributes().get("userName");
            
            if (userId == null) {
                return Mono.error(new RuntimeException("Session not authenticated"));
            }

            String content = message.getPayloadAsText();
            WebSocketMessageDto messageDto = new WebSocketMessageDto(
                content, 
                userId, 
                userName, 
                WebSocketMessageDto.MessageType.CHAT
            );

            // Broadcast to all connected sessions
            messageSink.tryEmitNext(messageDto);
            
            return Mono.empty();
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    private Mono<Void> sendMessage(WebSocketSession session, WebSocketMessageDto message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            return session.send(Mono.just(session.textMessage(json)));
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    private Mono<Void> sendErrorAndClose(WebSocketSession session, String errorMessage) {
        logger.warn("WebSocket error for session {}: {}", session.getId(), errorMessage);
        
        WebSocketMessageDto errorMsg = new WebSocketMessageDto(
            errorMessage, 
            "SYSTEM", 
            "System", 
            WebSocketMessageDto.MessageType.ERROR
        );
        
        return sendMessage(session, errorMsg)
            .then(session.close())
            .onErrorResume(e -> session.close());
    }

    private void removeSession(WebSocketSession session) {
        authenticatedSessions.remove(session.getId());
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            logger.info("WebSocket disconnected for user: {}", userId);
        }
    }

    // Method to broadcast messages to all authenticated sessions
    public void broadcastMessage(WebSocketMessageDto message) {
        messageSink.tryEmitNext(message);
    }

    public int getActiveSessionCount() {
        return authenticatedSessions.size();
    }
}