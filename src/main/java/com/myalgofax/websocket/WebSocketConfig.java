package com.myalgofax.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import com.myalgofax.websocket.KotakNeoWebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping webSocketHandlerMapping(AuthenticatedWebSocketHandler webSocketHandler, StrategyWebSocketHandler strategyHandler, KotakNeoWebSocketHandler kotakNeoHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/chat", webSocketHandler);
        map.put("/ws/strategies", strategyHandler);
        map.put("/ws/kotak/**", kotakNeoHandler);
        
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(map);
        handlerMapping.setOrder(1);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}