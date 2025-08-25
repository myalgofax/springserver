package com.myalgofax.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
    
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    public TokenBlacklistService() {
        // Clean expired tokens every hour
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::cleanExpiredTokens, 1, 1, TimeUnit.HOURS);
    }
    
    public void blacklistToken(String token, long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }
    
    public boolean isTokenBlacklisted(String token) {
        Long expiration = blacklistedTokens.get(token);
        if (expiration != null && System.currentTimeMillis() > expiration) {
            blacklistedTokens.remove(token);
            return false;
        }
        return expiration != null;
    }
    
    private void cleanExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }
}