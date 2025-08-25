package com.myalgofax.security.util.jwt;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.myalgofax.security.AuthenticationService;
import com.myalgofax.security.TokenBlacklistService;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private AuthenticationService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return chain.filter(exchange);
            }
            
            Claims claims = jwtUtil.validateAndExtractClaims(token);
            if (claims != null) {
                String email = claims.getSubject();
                String userId = claims.get("userId", String.class);
                
                if (email != null && userId != null) {
                    return authService.isUserActiveAndValid(email, userId)
                        .flatMap(isValid -> {
                            if (isValid) {
                                logger.debug("JWT authentication successful for user: {}", userId);
                                UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(email, userId, Collections.emptyList());
                                return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authentication))));
                            } else {
                                logger.warn("User validation failed for: {}", email);
                                return chain.filter(exchange);
                            }
                        })
                        .onErrorResume(e -> {
                            logger.debug("User validation error", e);
                            return chain.filter(exchange);
                        });
                }
            }
        }

        return chain.filter(exchange);
    }
}
