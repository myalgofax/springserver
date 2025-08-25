package com.myalgofax.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

import com.myalgofax.dto.UserDTO;
import com.myalgofax.repository.UserRepository;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.user.entity.User;

import reactor.core.publisher.Mono;

@Service
public class UserService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	private final JwtUtil jwtUtil;
	private final EmailService emailService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final SecureRandom secureRandom = new SecureRandom();
    
    public UserService(JwtUtil jwtUtil, EmailService emailService, UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }


    public Mono<User> register(UserDTO request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            return Mono.error(new IllegalArgumentException("Invalid user data"));
        }
        
        return userRepository.existsByEmail(request.getEmail())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.<User>error(new IllegalArgumentException("Email already exists"));
                }
                return generateUniqueUserId()
                    .map(userId -> {
                        User user = new User();
                        user.setUserId(userId);
                        user.setFirstName(request.getFirstName());
                        user.setLastName(request.getLastName());
                        user.setEmail(request.getEmail());
                        user.setPassword(encoder.encode(request.getPassword()));
                        user.setActive(true);
                        return user;
                    })
                    .flatMap(user -> userRepository.save(user))
                    .doOnNext(savedUser -> 
                        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getUserId())
                            .subscribe()
                    );
            })
            .onErrorResume(Exception.class, e -> {
                logger.error("User registration failed", e);
                return Mono.error(new RuntimeException("Registration failed", e));
            });
    }

    public Mono<Map<String, Object>> login(UserDTO request) {
        logger.debug("User login attempt");
        
        return userRepository.findByEmail(request.getEmail())
            .filter(user -> encoder.matches(request.getPassword(), user.getPassword()))
            .map(user -> {
                String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());
                logger.info("Login successful for user: {}", user.getUserId());
                
                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                response.put("token", token);
                
                return response;
            })
            .switchIfEmpty(Mono.fromRunnable(() -> logger.warn("Login failed for email: {}", request.getEmail()))
                .then(Mono.error(new RuntimeException("Invalid credentials"))));
    }

    public Mono<Map<String, Object>> mpinLogin(UserDTO request) {
        logger.debug("MPIN login attempt for email: {}", request.getEmail());
        logger.debug("Received MPIN: {}", request.getMpin());
        
        if (request.getMpin() == null || request.getMpin().length() != 64) {
            return Mono.error(new IllegalArgumentException("Invalid MPIN format"));
        }
        
        return userRepository.findByEmail(request.getEmail())
            .doOnNext(user -> logger.debug("Found user with stored MPIN: {}", user.getMpin() != null ? "exists" : "null"))
            .filter(user -> {
                boolean mpinExists = user.getMpin() != null;
                boolean mpinMatches = mpinExists && encoder.matches(request.getMpin(), user.getMpin());
                logger.debug("MPIN exists: {}, MPIN matches: {}", mpinExists, mpinMatches);
                return mpinMatches;
            })
            .map(user -> {
                String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());
                logger.info("MPIN login successful for user: {}", user.getUserId());
                
                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                response.put("token", token);
                
                return response;
            })
            .switchIfEmpty(Mono.fromRunnable(() -> logger.warn("MPIN login failed for email: {}", request.getEmail()))
                .then(Mono.error(new RuntimeException("Invalid MPIN or MPIN not set"))));
    }

    public Mono<User> setMpin(UserDTO request) {
        logger.debug("Setting MPIN for user: {}", request.toString());
        
        String mpin = request.getMpin();
        
        if (mpin == null) {
            return Mono.error(new IllegalArgumentException("MPIN is required"));
        }
        
        if (mpin.length() != 64) {
            return Mono.error(new IllegalArgumentException("Invalid MPIN format"));
        }
        
        return userRepository.findByEmail(request.getEmail())
            .flatMap(user -> {
                user.setMpin(encoder.encode(mpin));
                return userRepository.save(user);
            })
            .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
    
    private Mono<String> generateUniqueUserId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        int length = 8;

        return Mono.fromCallable(() -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = secureRandom.nextInt(characters.length());
                sb.append(characters.charAt(index));
            }
            return sb.toString();
        })
        .flatMap(userId -> 
            userRepository.existsByUserId(userId)
                .flatMap(exists -> exists ? generateUniqueUserId() : Mono.just(userId))
        );
    }

}

