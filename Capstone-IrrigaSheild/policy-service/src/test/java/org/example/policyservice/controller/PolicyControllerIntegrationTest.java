package org.example.policyservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.policyservice.config.SecurityConfig;
import org.example.policyservice.dto.response.PolicyResponse;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.security.TrustedHeaderAuthenticationFilter;
import org.example.policyservice.service.PolicyService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(PolicyController.class)
@Import({
        SecurityConfig.class,
        TrustedHeaderAuthenticationFilter.class
})
class PolicyControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PolicyService policyService;

    @Test
    void farmerShouldViewApprovedQuote() {

        when(policyService.getApprovedQuote(
                any(AuthenticatedUser.class),
                eq(1L)
        )).thenReturn(
                Mono.just(approvedQuote())
        );

        farmerGet("/api/policies/approved-quote/1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.applicationId")
                .isEqualTo(1)
                .jsonPath("$.premiumAmount")
                .isEqualTo(2600.0)
                .jsonPath("$.coverageAmount")
                .isEqualTo(60000.0)
                .jsonPath("$.paymentStatus")
                .isEqualTo("PENDING")
                .jsonPath("$.status")
                .isEqualTo("QUOTE_APPROVED");
    }

    @Test
    void farmerShouldAcceptApprovedQuote() {

        when(policyService.acceptQuote(
                any(AuthenticatedUser.class),
                eq(1L)
        )).thenReturn(
                Mono.just(activePolicy())
        );

        farmerPost(
                "/api/policies/approved-quote/1/accept"
        )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.policyId")
                .isEqualTo(1)
                .jsonPath("$.applicationId")
                .isEqualTo(1)
                .jsonPath("$.paymentStatus")
                .isEqualTo("SUCCESS")
                .jsonPath("$.status")
                .isEqualTo("ACTIVE");
    }

    @Test
    void underwriterShouldNotAccessFarmerPolicies() {

        webTestClient
                .get()
                .uri("/api/policies/my-policies")
                .header("X-User-Id", "3")
                .header(
                        "X-User-Email",
                        "underwriter@irrigashield.com"
                )
                .header(
                        "X-User-Roles",
                        "ROLE_UNDERWRITER"
                )
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void farmerShouldViewOwnPolicies() {

        when(policyService.getMyPolicies(
                any(AuthenticatedUser.class)
        )).thenReturn(
                Flux.just(activePolicy())
        );

        farmerGet("/api/policies/my-policies")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].policyId")
                .isEqualTo(1)
                .jsonPath("$[0].paymentStatus")
                .isEqualTo("SUCCESS")
                .jsonPath("$[0].status")
                .isEqualTo("ACTIVE");
    }

    private WebTestClient.RequestHeadersSpec<?> farmerGet(
            String url) {

        return webTestClient
                .get()
                .uri(url)
                .header("X-User-Id", "2")
                .header(
                        "X-User-Email",
                        "farmer@example.com"
                )
                .header(
                        "X-User-Roles",
                        "ROLE_FARMER"
                );
    }

    private WebTestClient.RequestHeadersSpec<?> farmerPost(
            String url) {

        return webTestClient
                .post()
                .uri(url)
                .header("X-User-Id", "2")
                .header(
                        "X-User-Email",
                        "farmer@example.com"
                )
                .header(
                        "X-User-Roles",
                        "ROLE_FARMER"
                );
    }

    private PolicyResponse approvedQuote() {

        return new PolicyResponse(
                null,
                1L,
                2L,
                "farmer@example.com",
                1L,
                1L,
                1L,
                "IrrigaShield Standard",
                new BigDecimal("60000.00"),
                new BigDecimal("2600.00"),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(120),
                "PENDING",
                "QUOTE_APPROVED",
                null
        );
    }

    private PolicyResponse activePolicy() {

        return new PolicyResponse(
                1L,
                1L,
                2L,
                "farmer@example.com",
                1L,
                1L,
                1L,
                "IrrigaShield Standard",
                new BigDecimal("60000.00"),
                new BigDecimal("2600.00"),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(120),
                "SUCCESS",
                "ACTIVE",
                LocalDateTime.now()
        );
    }
}