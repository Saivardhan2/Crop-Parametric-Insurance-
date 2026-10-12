package org.example.policyservice.repository;

import java.util.Collection;

import org.example.policyservice.entity.PolicyApplication;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyApplicationRepository
        extends ReactiveCrudRepository<PolicyApplication, Long> {

    Flux<PolicyApplication>
    findAllByFarmerUserIdOrderByCreatedAtDesc(
            Long farmerUserId
    );

    Mono<PolicyApplication>
    findByApplicationIdAndFarmerUserId(
            Long applicationId,
            Long farmerUserId
    );

    Mono<Boolean>
    existsByFarmerUserIdAndFarmIdAndStatusIn(
            Long farmerUserId,
            Long farmId,
            Collection<String> statuses
    );
    Flux<PolicyApplication>
    findAllByStatusOrderBySubmittedAtAsc(
            String status
    );
}