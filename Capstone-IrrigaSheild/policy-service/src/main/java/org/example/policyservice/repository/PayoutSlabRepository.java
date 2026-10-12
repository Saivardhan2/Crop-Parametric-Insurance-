package org.example.policyservice.repository;

import org.example.policyservice.entity.PayoutSlab;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayoutSlabRepository
        extends ReactiveCrudRepository<PayoutSlab, Long> {

    Flux<PayoutSlab> findAllByTriggerRuleIdAndStatusOrderByMinimumDurationHoursAsc(
            Long triggerRuleId,
            String status
    );

    Flux<PayoutSlab> findAllByTriggerRuleIdAndStatus(
            Long triggerRuleId,
            String status
    );

    Mono<Boolean> existsByTriggerRuleIdAndMinimumDurationHours(
            Long triggerRuleId,
            Integer minimumDurationHours
    );
}
