package org.example.farmservice.repository;

import org.example.farmservice.entity.FarmerProfile;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface FarmerProfileRepository
        extends ReactiveCrudRepository<FarmerProfile, Long> {

    Mono<FarmerProfile> findByAuthUserId(Long authUserId);

    Mono<FarmerProfile> findByEmailIgnoreCase(String email);

    Mono<Boolean> existsByAuthUserId(Long authUserId);

    Mono<Boolean> existsByEmailIgnoreCase(String email);
}