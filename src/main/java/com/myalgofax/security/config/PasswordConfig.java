package com.myalgofax.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
     BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12); // Higher strength for better security
    }
}