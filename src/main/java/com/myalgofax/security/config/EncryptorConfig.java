package com.myalgofax.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.myalgofax.security.util.Encryptor;
import com.myalgofax.user.entity.Broker;
import jakarta.annotation.PostConstruct;

@Configuration
public class EncryptorConfig {
    
    @Autowired
    private Encryptor encryptor;
    
    @PostConstruct
    public void init() {
        Broker.setEncryptor(encryptor);
    }
}