package com.myalgofax.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myalgofax.api.responce.ApiResponse;
import com.myalgofax.dto.KotakNeoAuthDto;
import com.myalgofax.dto.KotakNeoOrderDto;
import com.myalgofax.dto.KotakNeoQuoteDto;
import com.myalgofax.dto.KotakNeoWebSocketDto;
import com.myalgofax.service.KotakNeoService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/kotak")
public class KotakNeoController {
    
    private static final Logger logger = LoggerFactory.getLogger(KotakNeoController.class);
    
    @Autowired
    private KotakNeoService kotakNeoService;

    // Authentication Endpoints
    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> jwtLogin(@RequestBody KotakNeoAuthDto request) {
        logger.info("JWT login request received");
        
        return kotakNeoService.jwtLogin(request).map(data -> {
            String token = (String) data.get("access_token");
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "JWT login successful", data, token, false);
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(response);
        }).onErrorResume(e -> {
            logger.error("JWT login failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "JWT login failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        });
    }

    @PostMapping("/totp-login")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> totpLogin(@RequestBody KotakNeoAuthDto request) {
        logger.info("TOTP login request received");
        
        return kotakNeoService.totpLogin(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "TOTP login successful", data, null, true);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("TOTP login failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "TOTP login failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        });
    }

    @PostMapping("/validate-totp")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> totpValidate(@RequestBody KotakNeoAuthDto request) {
        logger.info("TOTP validation request received");
        
        return kotakNeoService.totpValidate(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "TOTP validation successful", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("TOTP validation failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "TOTP validation failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        });
    }

    @PostMapping("/qr-code")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> qrLink(@RequestBody KotakNeoAuthDto request) {
        logger.info("QR link request received");
        
        return kotakNeoService.qrLink(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "QR link generated successfully", data, null, true);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("QR link generation failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "QR link generation failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @PostMapping("/qr-session")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> qrSession(@RequestBody KotakNeoAuthDto request) {
        logger.info("QR session request received");
        
        return kotakNeoService.qrSession(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "QR session created successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("QR session creation failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "QR session creation failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> logout() {
        logger.info("Logout request received");
        
        return kotakNeoService.logout().map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Logout successful", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Logout failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Logout failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    // Order Management Endpoints
    @PostMapping("/place-order")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> placeOrder(@RequestBody KotakNeoOrderDto request) {
        logger.info("Place order request received");
        
        return kotakNeoService.placeOrder(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Order placed successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Place order failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Place order failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @PutMapping("/modify-order")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> modifyOrder(@RequestBody KotakNeoOrderDto request) {
        logger.info("Modify order request received");
        
        return kotakNeoService.modifyOrder(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Order modified successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Modify order failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Modify order failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @DeleteMapping("/cancel-order/{orderId}")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> cancelOrder(@PathVariable String orderId) {
        logger.info("Cancel order request received for orderId: {}", orderId);
        
        return kotakNeoService.cancelOrder(orderId).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Order cancelled successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Cancel order failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Cancel order failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/orders")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getOrders() {
        logger.info("Get orders request received");
        
        return kotakNeoService.getOrders().map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Orders fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get orders failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get orders failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/order-history/{orderId}")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getOrderHistory(@PathVariable String orderId) {
        logger.info("Get order history request received for orderId: {}", orderId);
        
        return kotakNeoService.getOrderHistory(orderId).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Order history fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get order history failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get order history failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    // Portfolio Endpoints
    @GetMapping("/positions")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getPositions() {
        logger.info("Get positions request received");
        
        return kotakNeoService.getPositions().map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Positions fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get positions failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get positions failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/holdings")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getHoldings() {
        logger.info("Get holdings request received");
        
        return kotakNeoService.getHoldings().map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Holdings fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get holdings failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get holdings failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/limits")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getLimits(
            @RequestParam(defaultValue = "ALL") String segment,
            @RequestParam(defaultValue = "ALL") String exchange,
            @RequestParam(defaultValue = "ALL") String product) {
        logger.info("Get limits request received");
        
        return kotakNeoService.getLimits(segment, exchange, product).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Limits fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get limits failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get limits failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    // Market Data Endpoints
    @PostMapping("/quotes")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getQuotes(@RequestBody KotakNeoQuoteDto request) {
        logger.info("Get quotes request received");
        
        return kotakNeoService.getQuotes(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Quotes fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get quotes failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get quotes failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/instruments")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getScripMaster(
            @RequestParam(required = false) String exchangeSegment) {
        logger.info("Get scrip master request received");
        
        return kotakNeoService.getScripMaster(exchangeSegment).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Scrip master fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get scrip master failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get scrip master failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    // Trade Endpoints
    @GetMapping("/trades")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getTrades(
            @RequestParam(required = false) String orderId) {
        logger.info("Get trades request received");
        
        return kotakNeoService.getTrades(orderId).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Trades fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get trades failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get trades failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    // WebSocket Management Endpoints
    @PostMapping("/websocket/unsubscribe")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> unsubscribeTokens(@RequestBody KotakNeoWebSocketDto request) {
        logger.info("Unsubscribe tokens request received");
        
        return kotakNeoService.unsubscribeTokens(request).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Tokens unsubscribed successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Unsubscribe tokens failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Unsubscribe tokens failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @PostMapping("/websocket/disconnect/{clientId}")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> disconnectClient(@PathVariable String clientId) {
        logger.info("Disconnect client request received for clientId: {}", clientId);
        
        return kotakNeoService.disconnectClient(clientId).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Client disconnected successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Disconnect client failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Disconnect client failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/websocket/status")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getWebSocketStatus() {
        logger.info("WebSocket status request received");
        
        return kotakNeoService.getWebSocketStatus().map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "WebSocket status fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get WebSocket status failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get WebSocket status failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }

    @GetMapping("/websocket/subscriptions/{clientId}")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getClientSubscriptions(@PathVariable String clientId) {
        logger.info("Get client subscriptions request received for clientId: {}", clientId);
        
        return kotakNeoService.getClientSubscriptions(clientId).map(data -> {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", 
                "Client subscriptions fetched successfully", data, null, false);
            return ResponseEntity.ok().body(response);
        }).onErrorResume(e -> {
            logger.error("Get client subscriptions failed", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
                "Get client subscriptions failed: " + e.getMessage(), null, null, false);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        });
    }
}