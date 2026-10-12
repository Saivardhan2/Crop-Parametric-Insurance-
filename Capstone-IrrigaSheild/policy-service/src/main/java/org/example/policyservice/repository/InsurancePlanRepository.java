package org.example.policyservice.repository;

import org.example.policyservice.entity.InsurancePlan;

import org.springframework.data.repository.reactive
        .ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InsurancePlanRepository
        extends ReactiveCrudRepository<InsurancePlan, Long> {

    Mono<Boolean> existsByPlanNameIgnoreCase(
            String planName
    );

    Flux<InsurancePlan> findAllByStatusOrderByCreatedAtDesc(
            String status
    );

    Flux<InsurancePlan> findAllByOrderByCreatedAtDesc();
}
