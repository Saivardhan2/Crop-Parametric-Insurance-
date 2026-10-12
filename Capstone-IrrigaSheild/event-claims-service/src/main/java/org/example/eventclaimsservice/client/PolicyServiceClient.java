package org.example.eventclaimsservice.client;

import org.example.eventclaimsservice.dto.PolicyClaimConfigurationResponse;
import org.example.eventclaimsservice.dto.PolicyDetails;
import org.example.eventclaimsservice.security.AuthenticatedUser;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class PolicyServiceClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyServiceClient.class);

    private final WebClient webClient;

    public PolicyServiceClient(
            @Qualifier("loadBalancedWebClientBuilder")
            WebClient.Builder webClientBuilder) {

        this.webClient = webClientBuilder
                .baseUrl("http://POLICY-SERVICE")
                .build();
    }

    public Mono<PolicyDetails> getActivePolicy(
            AuthenticatedUser user,
            Long policyId) {

        return webClient
                .get()
                .uri(
                        "/api/internal/policies/{policyId}",
                        policyId
                )
                .header(
                        "X-User-Id",
                        user.userId().toString()
                )
                .header(
                        "X-User-Email",
                        user.email()
                )
                .header(
                        "X-User-Roles",
                        "ROLE_DATA_PROVIDER"
                )
                .retrieve()
                .bodyToMono(PolicyDetails.class);
    }

    public Mono<PolicyClaimConfigurationResponse> getPolicyClaimConfiguration(
            AuthenticatedUser user,
            Long policyId) {

        return webClient
                .get()
                .uri(
                        "/api/internal/policies/{policyId}/claim-configuration",
                        policyId
                )
                .header(
                        "X-User-Id",
                        user.userId().toString()
                )
                .header(
                        "X-User-Email",
                        user.email()
                )
                .header(
                        "X-User-Roles",
                        "ROLE_DATA_PROVIDER"
                )
                .retrieve()
                .bodyToMono(PolicyClaimConfigurationResponse.class);
    }
}
