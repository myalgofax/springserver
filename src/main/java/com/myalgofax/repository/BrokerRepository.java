package com.myalgofax.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.myalgofax.user.entity.Broker;

import reactor.core.publisher.Mono;

@Repository
public interface BrokerRepository extends R2dbcRepository<Broker, Long> {
    Mono<Broker> findByUsername(String username);
    
    Mono<Boolean> existsByUsername(String username);

	Mono<Boolean> existsByUserId(String userId);

	Mono<Broker> findByUserId(String userId);

	Mono<Broker> findByUserIdAndBrokerCode(String userId, String brokerCode);
    
    @Modifying
    @Query("UPDATE brokers SET active_inv = :activeInv WHERE user_id = :userId AND broker_code = :brokerCode")
    Mono<Integer> updateActiveInvByUserIdAndBrokerCode(String userId, String brokerCode, String activeInv);
    
    @Modifying
    @Query("UPDATE brokers SET active_inv = 'N' WHERE user_id = :userId")
    Mono<Void> resetBrokerTokensByUserId(String userId);
}
