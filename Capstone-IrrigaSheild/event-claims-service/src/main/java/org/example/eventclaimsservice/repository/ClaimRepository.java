package org.example.eventclaimsservice.repository;

import java.time.LocalDate;
import java.util.Collection;

import org.example.eventclaimsservice.entity.Claim;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClaimRepository
        extends ReactiveCrudRepository<Claim, Long> {

    Mono<Boolean> existsByPolicyIdAndStatus(
            Long policyId,
            String status
    );

    Mono<Boolean> existsByPolicyIdAndEventDateAndStatus(
            Long policyId,
            LocalDate eventDate,
            String status
    );

    Mono<Boolean> existsByPolicyIdAndEventDateAndStatusIn(
            Long policyId,
            LocalDate eventDate,
            Collection<String> statuses
    );

    Flux<Claim> findAllByPolicyIdAndStatus(
            Long policyId,
            String status
    );

    Flux<Claim> findAllByFarmerUserIdOrderByCreatedAtDesc(
            Long farmerUserId
    );

    Flux<Claim> findAllByOrderByCreatedAtDesc();
}