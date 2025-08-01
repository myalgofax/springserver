package com.algo.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algo.api.responce.ApiResponse;
import com.algo.dto.UserDTO;
import com.algo.entity.User;
import com.algo.exceptions.ErrorResponse;
import com.algo.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // adjust for security in production
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Validated @RequestBody UserDTO request) {
        try {
            User registeredUser = userService.register(request);
            UserDTO userDTO = convertToDTO(registeredUser);
            
            ApiResponse<UserDTO> response = new ApiResponse<>(
                "success",
                "User registered successfully.",
                userDTO,
                null  // No token needed for registration
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (DataIntegrityViolationException e) {
            ApiResponse<UserDTO> errorResponse = new ApiResponse<>(
                "error",
                "Email is already registered.",
                null,
                null
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse<UserDTO> errorResponse = new ApiResponse<>(
                "error",
                e.getMessage(),
                null,
                null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    

    private UserDTO convertToDTO(User registeredUser) {
		// TODO Auto-generated method stub
		return null;
	}



	@PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody UserDTO request) {
        try {
            Map<String, Object> loginData = userService.login(request);
            String token = (String) loginData.get("token");
            User user = (User) loginData.get("user");

            ApiResponse<User> response = new ApiResponse<>(
                    "success",
                    "User logged in successfully.",
                    user,
                    token
            );
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(response);
        } catch (RuntimeException e) {
            ApiResponse<User> errorResponse = new ApiResponse<>(
                    "error",
                    "Invalid credentials",
                    null,
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}