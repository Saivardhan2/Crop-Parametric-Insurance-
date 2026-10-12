package org.example.policyservice.repository;

import org.example.policyservice.entity.Policy;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyRepository
        extends ReactiveCrudRepository<Policy, Long> {

    Mono<Boolean> existsByApplicationId(Long applicationId);

    Mono<Policy> findByApplicationId(Long applicationId);

    Flux<Policy> findAllByFarmerUserIdOrderByCreatedAtDesc(
            Long farmerUserId
    );
    Mono<Policy> findByPolicyIdAndStatus(
            Long policyId,
            String status
    );
}