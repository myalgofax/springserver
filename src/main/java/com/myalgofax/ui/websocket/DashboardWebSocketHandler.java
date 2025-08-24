package com.myalgofax.ui.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DashboardWebSocketHandler implements WebSocketHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Sinks.Many<DashboardEvent> eventSink = Sinks.many().multicast().onBackpressureBuffer();
    
    public static class DashboardEvent {
        private String type;
        private Object data;
        private long timestamp;
        
        public DashboardEvent(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessions.put(session.getId(), session);
        
        return session.send(
            eventSink.asFlux()
                .map(event -> {
                    try {
                        String json = objectMapper.writeValueAsString(event);
                        return session.textMessage(json);
                    } catch (Exception e) {
                        return session.textMessage("{\"error\":\"Serialization failed\"}");
                    }
                })
        ).doFinally(signalType -> sessions.remove(session.getId()));
    }
    
    public void broadcastStrategyUpdate(String strategyId, Object data) {
        eventSink.tryEmitNext(new DashboardEvent("STRATEGY_UPDATE", 
            Map.of("strategyId", strategyId, "data", data)));
    }
    
    public void broadcastPnLUpdate(Map<String, Double> pnlData) {
        eventSink.tryEmitNext(new DashboardEvent("PNL_UPDATE", pnlData));
    }
    
    public void broadcastRiskAlert(String message) {
        eventSink.tryEmitNext(new DashboardEvent("RISK_ALERT", Map.of("message", message)));
    }
    
    public void broadcastMarketData(String symbol, Map<String, Object> marketData) {
        eventSink.tryEmitNext(new DashboardEvent("MARKET_DATA", 
            Map.of("symbol", symbol, "data", marketData)));
    }
}