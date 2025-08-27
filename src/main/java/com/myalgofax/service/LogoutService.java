package com.myalgofax.service;

import org.springframework.http.HttpHeaders;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.myalgofax.exceptions.BrokerApiException;
import com.myalgofax.repository.BrokerRepository;
import com.myalgofax.security.TokenBlacklistService;
import com.myalgofax.security.util.jwt.JwtUtil;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class LogoutService {
	
	private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);

    private final TokenBlacklistService tokenBlacklistService;
    private final BrokerRepository brokerRepository;
    private final WebClient oauthWebClient;
    private final JwtUtil jwtUtil;

    public LogoutService(TokenBlacklistService tokenBlacklistService,
                        BrokerRepository brokerRepository,
                        @Qualifier("oauthWebClient") WebClient oauthWebClient,
                        JwtUtil jwtUtil) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.brokerRepository = brokerRepository;
        this.oauthWebClient = oauthWebClient;
        this.jwtUtil = jwtUtil;
    }

    public Mono<String> processLogout(String userId, String token) {
        return Mono.when(
                processTokenBlacklisting(token),
                resetBrokerTokens(userId),
                revokeOAuthToken(token)
            )
            .thenReturn("Logged out successfully");
    }

    private Mono<Void> processTokenBlacklisting(String token) {
        if (token == null) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
                Claims claims = jwtUtil.validateAndExtractClaims(token);
                if (claims != null) {
                    tokenBlacklistService.blacklistToken(token, claims.getExpiration().getTime());
                }
                return null;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                logger.warn("Failed to blacklist token", e);
                return Mono.empty();
            })
            .then();
    }

    private Mono<Void> resetBrokerTokens(String userId) {
        return Mono.fromRunnable(() -> 
                brokerRepository.resetBrokerTokensByUserId(userId)
            )
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private Mono<Map<String, String>> revokeOAuthToken(String token) {
       
    
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(token);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

        String revocationUrl = "https://napi.kotaksecurities.com/oauth2/revoke";
        
        return oauthWebClient.post()
            .uri(revocationUrl)
            
            .headers(headers -> {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + step1);
                headers.set("Sid", sid);
                headers.set("Auth", step2);
                headers.set(HttpHeaders.ACCEPT, "application/json");
                headers.set(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            })
            
            
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> Map.of("data", response))
            .onErrorResume(BrokerApiException.class, ex -> 
                Mono.just(Map.of("error", ex.getMessage()))
            )
            
            .onErrorResume(Exception.class, ex -> 
            Mono.just(Map.of("error", "Logout failed: " + ex.getMessage()))
        );
    }
}
