package org.example.policyservice.repository;

import org.example.policyservice.entity.TriggerRule;

import org.springframework.data.repository.reactive
        .ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TriggerRuleRepository
        extends ReactiveCrudRepository<TriggerRule, Long> {

    Flux<TriggerRule>
    findAllByPlanIdAndStatusOrderByCreatedAtDesc(
            Long planId,
            String status
    );

    Mono<Boolean>
    existsByPlanIdAndCropTypeIgnoreCaseAndGrowthStageIgnoreCase(
            Long planId,
            String cropType,
            String growthStage
    );
    Mono<TriggerRule>
    findByPlanIdAndCropTypeIgnoreCaseAndGrowthStageIgnoreCaseAndStatus(
            Long planId,
            String cropType,
            String growthStage,
            String status
    );
}