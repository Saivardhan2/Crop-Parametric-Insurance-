package org.example.farmservice.repository;

import org.example.farmservice.entity.IrrigationProfile;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface IrrigationProfileRepository
        extends ReactiveCrudRepository<IrrigationProfile, Long> {

    Mono<IrrigationProfile> findByFarmId(Long farmId);

    Mono<Boolean> existsByFarmId(Long farmId);
}