package com.myalgofax.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.myalgofax.user.entity.User;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByUserId(String userId);
	Mono<User> findByUserId(String userId);
	Mono<Boolean> existsByEmail(String email);
}
