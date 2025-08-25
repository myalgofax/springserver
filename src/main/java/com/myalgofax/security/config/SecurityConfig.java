package com.myalgofax.security.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.myalgofax.security.util.jwt.JwtFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	
	

    @Bean
     CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
           "http://localhost:*",
           "https://yourdomain.com"
        ));
        corsConfig.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With"
        ));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtFilter jwtFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disabled for API-only usage with JWT authentication
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/register","/api/auth/login","/api/auth/mpin-login","/api/auth/set-mpin","/api/auth/refresh-token",
                        		"/api/scrips/*",
                        		"/api/auth/logout",
                        		"/ws/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAfter(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
    
}