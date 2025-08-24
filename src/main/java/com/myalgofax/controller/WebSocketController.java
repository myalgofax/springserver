package com.myalgofax.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myalgofax.service.WebSocketService;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {

    private final WebSocketService webSocketService;

    public WebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "activeConnections", webSocketService.getActiveConnections(),
            "status", "WebSocket service is running"
        ));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> broadcastMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }
        
        webSocketService.broadcastNotification(message);
        return ResponseEntity.ok(Map.of("status", "Message broadcasted successfully"));
    }
}