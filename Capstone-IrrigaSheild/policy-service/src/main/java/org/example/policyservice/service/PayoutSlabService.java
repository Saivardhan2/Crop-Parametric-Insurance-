package org.example.policyservice.service;

import org.example.policyservice.dto.request
        .PayoutSlabRequest;
import org.example.policyservice.dto.response
        .PayoutSlabResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayoutSlabService {

    Mono<PayoutSlabResponse> createSlab(
            Long triggerRuleId,
            PayoutSlabRequest request
    );

    Flux<PayoutSlabResponse> getSlabs(
            Long triggerRuleId
    );
}