package com.myalgofax.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myalgofax.api.responce.ApiResponse;
import com.myalgofax.dto.UserDTO;
import com.myalgofax.repository.BrokerRepository;
import com.myalgofax.service.UserService;
import com.myalgofax.user.entity.User;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/auth")

public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
    @Autowired
    private UserService userService;
    

	@Autowired
	private BrokerRepository brokerRepository;
    
    

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<User>>> register(@Validated @RequestBody UserDTO request) {
        return userService.register(request)
            .map(user -> {
                ApiResponse<User> response = new ApiResponse<>(
                    "success",
                    "User registered successfully.",
                    user,
                    null,
                    false
                );
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            })
            .onErrorResume(DataIntegrityViolationException.class, e -> {
                ApiResponse<User> errorResponse = new ApiResponse<>(
                    "error",
                    "Email is already registered.",
                    null,
                    null,
                    false
                );
                return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
            })
            .onErrorResume(IllegalArgumentException.class, e -> {
                ApiResponse<User> errorResponse = new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null,
                    false
                );
                return Mono.just(ResponseEntity.badRequest().body(errorResponse));
            });
    }
    
    
	@PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<User>>> login(@RequestBody UserDTO request) {
        logger.debug("Enter in Login method");
        
        return userService.login(request)
            .map(loginData -> {
                String token = (String) loginData.get("token");
                User user = (User) loginData.get("user");
                
                ApiResponse<User> response = new ApiResponse<>(
                    "success",
                    "User logged in successfully.",
                    user,
                    token,
                    false
                );
                
                logger.debug("Login successful");
                return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(response);
            })
            .onErrorResume(IllegalArgumentException.class, e -> {
                logger.warn("Login failed - invalid input: {}", e.getMessage());
                ApiResponse<User> errorResponse = new ApiResponse<>(
                    "error",
                    "Invalid credentials",
                    null,
                    null,
                    false
                );
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
            })
            .onErrorResume(RuntimeException.class, e -> {
                logger.error("Login failed", e);
                ApiResponse<User> errorResponse = new ApiResponse<>(
                    "error",
                    "Login failed",
                    null,
                    null,
                    false
                );
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
            });
    }
	
	 @PostMapping("/logout")
	    public Mono<ResponseEntity<ApiResponse<Object>>> logout() {
	        return ReactiveSecurityContextHolder.getContext()
	            .map(ctx -> (String) ctx.getAuthentication().getCredentials())
	            .flatMap(userId -> 
	                Mono.fromCallable(() -> {
	                    brokerRepository.resetBrokerTokensByUserId(userId);
	                    return "Logged out successfully";
	                })
	                .subscribeOn(Schedulers.boundedElastic())
	            )
	            .map(message -> ResponseEntity.ok(new ApiResponse<>("success", message, null, null, true)))
	            .onErrorResume(e -> {
	                logger.error("Logout failed", e);
	                return Mono.just(ResponseEntity.ok(new ApiResponse<>("success", "Logged out", null, null, true)));
	            });
	    }
	
	
	
}