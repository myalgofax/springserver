package com.myalgofax.controller;

import java.util.Map;

import org.slf4j.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myalgofax.api.responce.ApiResponse;
import com.myalgofax.dto.UserDTO;
import com.myalgofax.repository.BrokerRepository;
import com.myalgofax.security.TokenBlacklistService;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.service.LogoutService;
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
	
	@Autowired
	private TokenBlacklistService tokenBlacklistService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private  LogoutService logoutService;
    
    

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
	
	 

	@PostMapping("/mpin-login")
	public Mono<ResponseEntity<ApiResponse<User>>> mpinLogin(@RequestBody Map<String, String> requestMap) {
		logger.info("MPIN Login raw request: {}", requestMap);
		
		UserDTO request = new UserDTO();
		request.setEmail(requestMap.get("email"));
		request.setMpin(requestMap.get("mpin"));
		
		logger.debug("Enter in MPIN Login method");
		
		return userService.mpinLogin(request)
			.map(loginData -> {
				String token = (String) loginData.get("token");
				User user = (User) loginData.get("user");
				
				ApiResponse<User> response = new ApiResponse<>(
					"success",
					"User logged in successfully with MPIN.",
					user,
					token,
					false
				);
				
				logger.info("MPIN login successful - returning response with token",response);
				return ResponseEntity.ok()
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.body(response);
			})
			.onErrorResume(IllegalArgumentException.class, e -> {
				logger.warn("MPIN login failed - invalid input: {}", e.getMessage());
				ApiResponse<User> errorResponse = new ApiResponse<>(
					"error",
					"Invalid MPIN or MPIN not set",
					null,
					null,
					false
				);
				return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
			})
			.onErrorResume(RuntimeException.class, e -> {
				logger.error("MPIN login failed", e);
				ApiResponse<User> errorResponse = new ApiResponse<>(
					"error",
					"MPIN login failed",
					null,
					null,
					false
				);
				return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
			});
	}

	@PostMapping("/set-mpin")
	public Mono<ResponseEntity<ApiResponse<User>>> setMpin(@RequestBody Map<String, String> requestMap) {
		logger.info("Raw request: {}", requestMap);
		
		UserDTO request = new UserDTO();
		request.setEmail(requestMap.get("email"));
		request.setMpin(requestMap.get("mpin"));
		
		logger.debug("Enter in Set MPIN method");
		logger.info("request data",request.getNewMpin());
		logger.info("request getConfirmMpin: ",request.getConfirmMpin());
		
		return userService.setMpin(request)
			.map(user -> {
				ApiResponse<User> response = new ApiResponse<>(
					"success",
					"MPIN set successfully.",
					user,
					null,
					false
				);
				return ResponseEntity.ok().body(response);
			})
			.onErrorResume(IllegalArgumentException.class, e -> {
				logger.warn("Set MPIN failed - invalid input: {}", e.getMessage());
				ApiResponse<User> errorResponse = new ApiResponse<>(
					"error",
					e.getMessage(),
					null,
					null,
					false
				);
				return Mono.just(ResponseEntity.badRequest().body(errorResponse));
			})
			.onErrorResume(RuntimeException.class, e -> {
				logger.error("Set MPIN failed", e);
				ApiResponse<User> errorResponse = new ApiResponse<>(
					"error",
					"Failed to set MPIN",
					null,
					null,
					false
				);
				return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
			});
	}

	@PostMapping("/refresh-token")
	public Mono<ResponseEntity<ApiResponse<String>>> refreshToken(@RequestBody Map<String, String> request) {
		String token = request.get("token");
		if (token == null) {
			return Mono.just(ResponseEntity.badRequest()
				.body(new ApiResponse<>("error", "Token required", null, null, false)));
		}
		
		String newToken = jwtUtil.refreshToken(token);
		if (newToken != null) {
			tokenBlacklistService.blacklistToken(token, System.currentTimeMillis() + 86400000);
			return Mono.just(ResponseEntity.ok()
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken)
				.body(new ApiResponse<>("success", "Token refreshed", newToken, newToken, false)));
		}
		
		return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new ApiResponse<>("error", "Invalid token", null, null, false)));
	}
	
	
	
	
	@PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Object>>> logout(@RequestBody(required = false) Map<String, String> request) {
        logger.info("enter in LOGOUT");
		return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Authentication authentication = securityContext.getAuthentication();
                if (authentication == null || authentication.getCredentials() == null) {
                    return Mono.error(new RuntimeException("No authentication found"));
                }
                
                String userId = (String) authentication.getCredentials();
                String token = request != null ? request.get("token") : null;
                
                return logoutService.processLogout(userId, token)
                    .map(message -> ResponseEntity.ok(new ApiResponse<>("success", message, null, null, true)));
            })
            .onErrorResume(e -> {
                logger.error("Logout failed", e);
                return Mono.just(ResponseEntity.ok(new ApiResponse<>("success", "Logged out", null, null, true)));
            });
    }
}