package com.myalgofax.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class KotakNeoWebSocketHandler implements WebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(KotakNeoWebSocketHandler.class);
    private final ConcurrentHashMap<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    private final Sinks.Many<String> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String clientId = extractClientId(session);
        clientSessions.put(clientId, session);
        logger.info("Client connected: {}", clientId);
        
        Mono<Void> input = session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(message -> {
                logger.info("Message from client {}: {}", clientId, message);
                forwardToExternalWebSocket(message);
            })
            .then();
        
        Flux<WebSocketMessage> output = messageSink.asFlux()
            .map(session::textMessage);
        
        return session.send(output)
            .and(input)
            .doFinally(signalType -> {
                clientSessions.remove(clientId);
                logger.info("Client disconnected: {}", clientId);
            });
    }
    
    private void forwardToExternalWebSocket(String message) {
        logger.info("Forwarding to external WebSocket: {}", message);
        messageSink.tryEmitNext("{\"type\":\"response\",\"data\":\"Message received\"}");
    }
    
    private String extractClientId(WebSocketSession session) {
        return session.getId();
    }
}