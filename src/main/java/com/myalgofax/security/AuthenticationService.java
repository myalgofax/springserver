package com.myalgofax.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myalgofax.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class AuthenticationService {
    
    @Autowired
    private UserRepository userRepository;
    
    public Mono<Boolean> isUserActiveAndValid(String email, String userId) {
        return userRepository.findByEmail(email)
            .map(user -> user.isActive() && user.getUserId().equals(userId))
            .defaultIfEmpty(false);
    }
}