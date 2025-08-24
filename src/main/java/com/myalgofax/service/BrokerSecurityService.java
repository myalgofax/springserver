package com.myalgofax.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.myalgofax.security.util.Encryptor;
import com.myalgofax.user.entity.Broker;
import com.myalgofax.repository.BrokerRepository;
import reactor.core.publisher.Mono;

@Service
public class BrokerSecurityService {
    
    @Autowired
    private Encryptor encryptor;
    
    @Autowired
    private BrokerRepository brokerRepository;
    
    public Mono<Broker> saveBrokerWithEncryption(Broker broker) {
        // Sensitive fields are automatically encrypted in the entity setters
        return brokerRepository.save(broker);
    }
    
    public String encryptSensitiveData(String plainText) {
        return encryptor.encode(plainText);
    }
    
    public String decryptSensitiveData(String encryptedText) {
        return encryptor.decode(encryptedText);
    }
}