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
import com.myalgofax.dto.StrategyConfigDto;
import com.myalgofax.dto.SignalDto;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.strategy.StrategyExecutionEngine;
import com.myalgofax.strategy.StrategyInstance;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class StrategyWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(StrategyWebSocketHandler.class);
    
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final StrategyExecutionEngine executionEngine;
    private final Map<String, WebSocketSession> strategySessions = new ConcurrentHashMap<>();

    public StrategyWebSocketHandler(JwtUtil jwtUtil, ObjectMapper objectMapper, StrategyExecutionEngine executionEngine) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.executionEngine = executionEngine;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
            .cast(WebSocketMessage.class)
            .take(1)
            .flatMap(message -> authenticateAndProcessStrategy(session, message))
            .then(handleStrategySession(session))
            .doOnTerminate(() -> removeSession(session))
            .onErrorResume(error -> {
                logger.error("Strategy WebSocket error: {}", error.getMessage());
                return sendErrorAndClose(session, error.getMessage());
            });
    }

    private Mono<Void> authenticateAndProcessStrategy(WebSocketSession session, WebSocketMessage message) {
        try {
            String payload = message.getPayloadAsText();
            StrategyConfigDto config = objectMapper.readValue(payload, StrategyConfigDto.class);
            
            if (config.getUserToken() == null || !jwtUtil.validateToken(config.getUserToken())) {
                return sendErrorAndClose(session, "Invalid or missing authentication token");
            }

            String userId = jwtUtil.extractUserId(config.getUserToken());
            if (userId == null) {
                return sendErrorAndClose(session, "Invalid token: missing user information");
            }

            session.getAttributes().put("userId", userId);
            session.getAttributes().put("strategyId", config.getStrategyId());
            strategySessions.put(session.getId(), session);

            return executionEngine.deployStrategy(config, userId)
                .flatMap(strategyId -> {
                    String response = String.format("{\"status\":\"deployed\",\"strategyId\":\"%s\"}", strategyId);
                    return session.send(Mono.just(session.textMessage(response)));
                })
                .onErrorResume(error -> sendErrorAndClose(session, "Strategy deployment failed: " + error.getMessage()));

        } catch (Exception e) {
            logger.error("Error processing strategy configuration: {}", e.getMessage());
            return sendErrorAndClose(session, "Invalid strategy configuration format");
        }
    }

    private Mono<Void> handleStrategySession(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        
        // Handle incoming strategy updates
        Mono<Void> input = session.receive()
            .skip(1)
            .flatMap(message -> processStrategyUpdate(session, message))
            .then();

        // Send strategy signals and updates
        Flux<WebSocketMessage> output = executionEngine.getSignalStream()
            .filter(signal -> {
                StrategyInstance strategy = executionEngine.getActiveStrategies().get(signal.getStrategyId());
                return strategy != null && strategy.getUserId().equals(userId);
            })
            .map(signal -> {
                try {
                    return session.textMessage(objectMapper.writeValueAsString(signal));
                } catch (Exception e) {
                    logger.error("Error serializing signal: {}", e.getMessage());
                    return session.textMessage("{\"error\":\"Signal serialization failed\"}");
                }
            });

        return session.send(output).and(input);
    }

    private Mono<Void> processStrategyUpdate(WebSocketSession session, WebSocketMessage message) {
        try {
            String payload = message.getPayloadAsText();
            StrategyConfigDto config = objectMapper.readValue(payload, StrategyConfigDto.class);
            
            return executionEngine.updateStrategy(config.getStrategyId(), config)
                .then(Mono.fromRunnable(() -> {
                    String response = String.format("{\"status\":\"updated\",\"strategyId\":\"%s\"}", config.getStrategyId());
                    session.send(Mono.just(session.textMessage(response))).subscribe();
                }));

        } catch (Exception e) {
            logger.error("Error processing strategy update: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    private Mono<Void> sendErrorAndClose(WebSocketSession session, String errorMessage) {
        logger.warn("Strategy WebSocket error: {}", errorMessage);
        String errorJson = String.format("{\"error\":\"%s\"}", errorMessage);
        return session.send(Mono.just(session.textMessage(errorJson)))
            .then(session.close())
            .onErrorResume(e -> session.close());
    }

    private void removeSession(WebSocketSession session) {
        strategySessions.remove(session.getId());
        String strategyId = (String) session.getAttributes().get("strategyId");
        if (strategyId != null) {
            executionEngine.deactivateStrategy(strategyId).subscribe();
            logger.info("Strategy session disconnected: {}", strategyId);
        }
    }

    public int getActiveSessionCount() {
        return strategySessions.size();
    }
}