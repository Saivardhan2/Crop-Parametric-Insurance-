package org.example.farmservice.repository;

import org.example.farmservice.entity.Crop;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CropRepository
        extends ReactiveCrudRepository<Crop, Long> {

    Flux<Crop> findAllByFarmIdOrderByCreatedAtDesc(
            Long farmId
    );

    Mono<Crop> findByCropIdAndFarmId(
            Long cropId,
            Long farmId
    );
}