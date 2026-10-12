package org.example.policyservice.service;

import org.example.policyservice.dto.request
        .TriggerRuleRequest;
import org.example.policyservice.dto.response
        .TriggerRuleResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TriggerRuleService {

    Mono<TriggerRuleResponse> createRule(
            Long planId,
            TriggerRuleRequest request
    );

    Flux<TriggerRuleResponse> getRulesForPlan(
            Long planId
    );
}