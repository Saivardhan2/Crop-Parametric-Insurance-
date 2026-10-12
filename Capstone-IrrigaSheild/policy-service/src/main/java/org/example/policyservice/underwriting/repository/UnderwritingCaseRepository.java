package org.example.policyservice.underwriting.repository;

import org.example.policyservice.underwriting.entity.UnderwritingCase;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UnderwritingCaseRepository
        extends ReactiveCrudRepository<UnderwritingCase, Long> {

    Mono<UnderwritingCase> findByApplicationId(
            Long applicationId
    );

    Mono<Boolean> existsByApplicationId(
            Long applicationId
    );

    Flux<UnderwritingCase>
    findAllByUnderwriterUserIdOrderByCreatedAtDesc(
            Long underwriterUserId
    );
    reactor.core.publisher.Flux<org.example.policyservice.underwriting.entity.UnderwritingCase> findAllByOrderByCreatedAtDesc();
}
