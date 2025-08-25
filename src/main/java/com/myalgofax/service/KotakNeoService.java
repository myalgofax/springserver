package com.myalgofax.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.myalgofax.dto.KotakNeoAuthDto;
import com.myalgofax.dto.KotakNeoOrderDto;
import com.myalgofax.dto.KotakNeoQuoteDto;
import com.myalgofax.dto.KotakNeoWebSocketDto;
import com.myalgofax.security.util.jwt.JwtUtil;

import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@PropertySource("classpath:kotak-neo.properties")
public class KotakNeoService {
    
    private static final Logger logger = LoggerFactory.getLogger(KotakNeoService.class);
    private final WebClient webClient;
    
    @Value("${kotak.neo.base.url}")
    private String baseUrl;
    
    @Value("${kotak.neo.api.auth.jwt-login}")
    private String jwtLoginUri;
    
    @Value("${kotak.neo.api.auth.totp-login}")
    private String totpLoginUri;
    
    @Value("${kotak.neo.api.auth.totp-validate}")
    private String totpValidateUri;
    
    @Value("${kotak.neo.api.auth.qr-link}")
    private String qrLinkUri;
    
    @Value("${kotak.neo.api.auth.qr-session}")
    private String qrSessionUri;
    
    @Value("${kotak.neo.api.auth.logout}")
    private String logoutUri;
    
    @Value("${kotak.neo.api.orders.place}")
    private String placeOrderUri;
    
    @Value("${kotak.neo.api.orders.modify}")
    private String modifyOrderUri;
    
    @Value("${kotak.neo.api.orders.cancel}")
    private String cancelOrderUri;
    
    @Value("${kotak.neo.api.orders.list}")
    private String ordersUri;
    
    @Value("${kotak.neo.api.orders.history}")
    private String orderHistoryUri;
    
    @Value("${kotak.neo.api.portfolio.positions}")
    private String positionsUri;
    
    @Value("${kotak.neo.api.portfolio.holdings}")
    private String holdingsUri;
    
    @Value("${kotak.neo.api.portfolio.limits}")
    private String limitsUri;
    
    @Value("${kotak.neo.api.market.quotes}")
    private String quotesUri;
    
    @Value("${kotak.neo.api.market.scrip-master}")
    private String scripMasterUri;
    
    @Value("${kotak.neo.api.trades}")
    private String tradesUri;
    
    @Value("${kotak.neo.api.websocket.unsubscribe}")
    private String unsubscribeUri;
    
    @Value("${kotak.neo.api.websocket.disconnect}")
    private String disconnectUri;
    
    @Value("${kotak.neo.api.websocket.status}")
    private String websocketStatusUri;
    
    @Value("${kotak.neo.api.websocket.subscriptions}")
    private String subscriptionsUri;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public KotakNeoService() {
        this.webClient = WebClient.builder().build();
    }

    // Authentication Methods
    public Mono<Map<String, Object>> jwtLogin(KotakNeoAuthDto request) {
        logger.info("Calling external Kotak Neo JWT login API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("user_id", request.getUserId());
        requestBody.put("ucc", request.getUcc());
        requestBody.put("consumer_key", request.getConsumerKey());
        requestBody.put("consumer_secret", request.getConsumerSecret());
        requestBody.put("environment", request.getEnvironment());
        
        return webClient.post()
            .uri(baseUrl + jwtLoginUri)
            .headers(headers -> addAuthHeaders(headers, request.getUserId()))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("JWT login successful"))
            .doOnError(error -> logger.error("JWT login failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> totpLogin(KotakNeoAuthDto request) {
        logger.info("Calling external Kotak Neo TOTP login API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("mobile_number", request.getMobileNumber());
        requestBody.put("ucc", request.getUcc());
        requestBody.put("totp", request.getTotp());
        requestBody.put("consumer_key", request.getConsumerKey());
        requestBody.put("consumer_secret", request.getConsumerSecret());
        requestBody.put("environment", request.getEnvironment());
        
        return webClient.post()
            .uri(baseUrl + totpLoginUri)
            .headers(headers -> addAuthHeaders(headers, request.getUserId()))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("TOTP login successful"))
            .doOnError(error -> logger.error("TOTP login failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> totpValidate(KotakNeoAuthDto request) {
        logger.info("Calling external Kotak Neo TOTP validate API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("mpin", request.getMpin());
        
        return webClient.post()
            .uri(baseUrl + totpValidateUri)
            .headers(headers -> addAuthHeaders(headers, request.getUserId()))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("TOTP validation successful"))
            .doOnError(error -> logger.error("TOTP validation failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> qrLink(KotakNeoAuthDto request) {
        logger.info("Calling external Kotak Neo QR link API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("ucc", request.getUcc());
        requestBody.put("consumer_key", request.getConsumerKey());
        requestBody.put("consumer_secret", request.getConsumerSecret());
        requestBody.put("environment", request.getEnvironment());
        
        return webClient.post()
            .uri(baseUrl + qrLinkUri)
            .headers(headers -> addAuthHeaders(headers, request.getUcc()))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("QR link generation successful"))
            .doOnError(error -> logger.error("QR link generation failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> qrSession(KotakNeoAuthDto request) {
        logger.info("Calling external Kotak Neo QR session API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("ott", request.getOtt());
        requestBody.put("ucc", request.getUcc());
        
        return webClient.post()
            .uri(baseUrl + qrSessionUri)
            .headers(headers -> addAuthHeaders(headers, request.getUcc()))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("QR session creation successful"))
            .doOnError(error -> logger.error("QR session creation failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> logout() {
        logger.info("Calling external Kotak Neo logout API");
        
        return webClient.post()
            .uri(baseUrl + logoutUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Logout successful"))
            .doOnError(error -> logger.error("Logout failed: {}", error.getMessage()));
    }

    // Order Management Methods
    public Mono<Map<String, Object>> placeOrder(KotakNeoOrderDto request) {
        logger.info("Calling external Kotak Neo place order API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("exchange_segment", request.getExchangeSegment());
        requestBody.put("product", request.getProduct());
        requestBody.put("price", request.getPrice());
        requestBody.put("order_type", request.getOrderType());
        requestBody.put("quantity", request.getQuantity());
        requestBody.put("validity", request.getValidity());
        requestBody.put("trading_symbol", request.getTradingSymbol());
        requestBody.put("transaction_type", request.getTransactionType());
        
        return webClient.post()
            .uri(baseUrl + placeOrderUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Order placed successfully"))
            .doOnError(error -> logger.error("Place order failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> modifyOrder(KotakNeoOrderDto request) {
        logger.info("Calling external Kotak Neo modify order API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("order_id", request.getOrderId());
        requestBody.put("price", request.getPrice());
        requestBody.put("quantity", request.getQuantity());
        requestBody.put("order_type", request.getOrderType());
        
        return webClient.put()
            .uri(baseUrl + modifyOrderUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Order modified successfully"))
            .doOnError(error -> logger.error("Modify order failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> cancelOrder(String orderId) {
        logger.info("Calling external Kotak Neo cancel order API for orderId: {}", orderId);
        
        return webClient.delete()
            .uri(baseUrl + cancelOrderUri, orderId)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Order cancelled successfully"))
            .doOnError(error -> logger.error("Cancel order failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getOrders() {
        logger.info("Calling external Kotak Neo get orders API");
        
        return webClient.get()
            .uri(baseUrl + ordersUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Orders fetched successfully"))
            .doOnError(error -> logger.error("Get orders failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getOrderHistory(String orderId) {
        logger.info("Calling external Kotak Neo get order history API for orderId: {}", orderId);
        
        return webClient.get()
            .uri(baseUrl + orderHistoryUri, orderId)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Order history fetched successfully"))
            .doOnError(error -> logger.error("Get order history failed: {}", error.getMessage()));
    }

    // Portfolio Methods
    public Mono<Map<String, Object>> getPositions() {
        logger.info("Calling external Kotak Neo get positions API");
        
        return webClient.get()
            .uri(baseUrl + positionsUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Positions fetched successfully"))
            .doOnError(error -> logger.error("Get positions failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getHoldings() {
        logger.info("Calling external Kotak Neo get holdings API");
        
        return webClient.get()
            .uri(baseUrl + holdingsUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Holdings fetched successfully"))
            .doOnError(error -> logger.error("Get holdings failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getLimits(String segment, String exchange, String product) {
        logger.info("Calling external Kotak Neo get limits API");
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(baseUrl + limitsUri)
                .queryParam("segment", segment)
                .queryParam("exchange", exchange)
                .queryParam("product", product)
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Limits fetched successfully"))
            .doOnError(error -> logger.error("Get limits failed: {}", error.getMessage()));
    }

    // Market Data Methods
    public Mono<Map<String, Object>> getQuotes(KotakNeoQuoteDto request) {
        logger.info("Calling external Kotak Neo get quotes API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("instrument_tokens", request.getInstrumentTokens());
        requestBody.put("quote_type", request.getQuoteType());
        
        return webClient.post()
            .uri(baseUrl + quotesUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Quotes fetched successfully"))
            .doOnError(error -> logger.error("Get quotes failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getScripMaster(String exchangeSegment) {
        logger.info("Calling external Kotak Neo get scrip master API");
        
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path(baseUrl + scripMasterUri);
                if (exchangeSegment != null) {
                    builder.queryParam("exchange_segment", exchangeSegment);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Scrip master fetched successfully"))
            .doOnError(error -> logger.error("Get scrip master failed: {}", error.getMessage()));
    }

    // Trade Methods
    public Mono<Map<String, Object>> getTrades(String orderId) {
        logger.info("Calling external Kotak Neo get trades API");
        
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path(baseUrl + tradesUri);
                if (orderId != null) {
                    builder.queryParam("order_id", orderId);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Trades fetched successfully"))
            .doOnError(error -> logger.error("Get trades failed: {}", error.getMessage()));
    }

    // WebSocket Management Methods
    public Mono<Map<String, Object>> unsubscribeTokens(KotakNeoWebSocketDto request) {
        logger.info("Calling external Kotak Neo unsubscribe tokens API");
        
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("client_id", request.getClientId());
        requestBody.put("instrument_tokens", request.getInstrumentTokens());
        
        return webClient.post()
            .uri(baseUrl + unsubscribeUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .bodyValue(addKotakTokenToBody(requestBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Tokens unsubscribed successfully"))
            .doOnError(error -> logger.error("Unsubscribe tokens failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> disconnectClient(String clientId) {
        logger.info("Calling external Kotak Neo disconnect client API for clientId: {}", clientId);
        
        return webClient.post()
            .uri(baseUrl + disconnectUri, clientId)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Client disconnected successfully"))
            .doOnError(error -> logger.error("Disconnect client failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getWebSocketStatus() {
        logger.info("Calling external Kotak Neo WebSocket status API");
        
        return webClient.get()
            .uri(baseUrl + websocketStatusUri)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("WebSocket status fetched successfully"))
            .doOnError(error -> logger.error("Get WebSocket status failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getClientSubscriptions(String clientId) {
        logger.info("Calling external Kotak Neo client subscriptions API for clientId: {}", clientId);
        
        return webClient.get()
            .uri(baseUrl + subscriptionsUri, clientId)
            .headers(headers -> addAuthHeaders(headers, null))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .doOnSuccess(response -> logger.info("Client subscriptions fetched successfully"))
            .doOnError(error -> logger.error("Get client subscriptions failed: {}", error.getMessage()));
    }
    
    private void addAuthHeaders(org.springframework.http.HttpHeaders headers, String userId) {
        try {
            String userToken = getCurrentUserToken();
            if (userToken != null) {
                headers.set("Authorization", "Bearer " + userToken);
            }
        } catch (Exception e) {
            logger.warn("Failed to add auth headers: {}", e.getMessage());
        }
    }
    
    private Map<String, Object> addKotakTokenToBody(Map<String, Object> originalBody) {
        try {
            String userToken = getCurrentUserToken();
            if (userToken != null) {
                String kotakToken = jwtUtil.extractClaim(userToken, "kotakTokenStep2");
                if (kotakToken != null) {
                    originalBody.put("kotakToken", kotakToken);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to add kotak token to body: {}", e.getMessage());
        }
        return originalBody;
    }
    
    private String getCurrentUserToken() {
        return null;
    }
}