package com.algo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.algo.dto.UserDTO;
import com.algo.entity.User;
import com.algo.repository.UserRepository;
import com.algo.security.util.jwt.JwtUtil;

@Service
public class UserService {
	
	@Autowired
	private  JwtUtil jwtUtil;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
    private  UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();



    public User register(UserDTO request) {
        User user = new User();
        user.setUserId(generateUniqueUserId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName(), savedUser.getUserId());
        
        return savedUser;
    }


    public Map<String, Object> login(UserDTO request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isPresent() && encoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);
            return response;
        }
        throw new RuntimeException("Invalid credentials");
    }
    
    private String generateUniqueUserId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        int length = 8;

        String userId;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = (int) (Math.random() * characters.length());
                sb.append(characters.charAt(index));
            }
            userId = sb.toString();
        } while (userRepository.existsByUserId(userId)); // Ensure uniqueness

        return userId;
    }

}

