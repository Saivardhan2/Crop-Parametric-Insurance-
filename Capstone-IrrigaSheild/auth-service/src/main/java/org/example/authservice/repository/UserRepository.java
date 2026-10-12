package org.example.authservice.repository;


import org.example.authservice.entity.User;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface UserRepository
        extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByEmailIgnoreCase(String email);

    Mono<Boolean> existsByEmailIgnoreCase(String email);
}