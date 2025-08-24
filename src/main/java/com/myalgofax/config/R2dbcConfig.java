package com.myalgofax.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {
    // R2DBC configuration with auditing enabled for @CreatedDate and @LastModifiedDate
    // ConnectionFactory is auto-configured by Spring Boot from application properties
}