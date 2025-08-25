package com.myalgofax.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements WebFilter {
    
    private final ConcurrentHashMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
    private final Duration windowDuration = Duration.ofMinutes(15);
    private final int maxAttempts = 5;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        if (path.contains("/login") || path.contains("/mpin-login")) {
            String clientIp = getClientIp(exchange);
            AtomicInteger count = attempts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
            
            if (count.incrementAndGet() > maxAttempts) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }
            
            // Reset counter after window
            Mono.delay(windowDuration).subscribe(v -> attempts.remove(clientIp));
        }
        
        return chain.filter(exchange);
    }
    
    private String getClientIp(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-Forwarded-For") != null
            ? exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
            : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}