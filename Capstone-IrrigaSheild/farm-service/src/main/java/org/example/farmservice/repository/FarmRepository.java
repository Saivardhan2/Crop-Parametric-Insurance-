package org.example.farmservice.repository;

import org.example.farmservice.entity.Farm;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FarmRepository
        extends ReactiveCrudRepository<Farm, Long> {

    Flux<Farm> findAllByFarmerIdOrderByCreatedAtDesc(
            Long farmerId
    );

    Mono<Farm> findByFarmIdAndFarmerId(
            Long farmId,
            Long farmerId
    );

    Mono<Boolean> existsByFarmerIdAndFarmNameIgnoreCase(
            Long farmerId,
            String farmName
    );
}