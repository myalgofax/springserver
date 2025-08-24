package com.myalgofax.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreaker mlServiceCircuitBreaker() {
        return CircuitBreaker.of("mlService", io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build());
    }
}