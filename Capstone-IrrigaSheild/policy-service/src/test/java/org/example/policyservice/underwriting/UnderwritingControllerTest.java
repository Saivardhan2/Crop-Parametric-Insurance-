package org.example.policyservice.underwriting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.controller.UnderwritingController;
import org.example.policyservice.underwriting.dto.request.DecisionRequest;
import org.example.policyservice.underwriting.dto.response.UnderwritingResponse;
import org.example.policyservice.underwriting.service.UnderwritingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UnderwritingControllerTest {

    private UnderwritingService underwritingService;
    private UnderwritingController controller;
    private AuthenticatedUser underwriter;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        underwritingService = mock(UnderwritingService.class);
        controller = new UnderwritingController(underwritingService);
        underwriter = new AuthenticatedUser(10L, "uw@irrigashield.com");
        auth = new UsernamePasswordAuthenticationToken(underwriter, null, Collections.emptyList());
    }

    @Test
    void shouldGetPendingAndAllCases() {
        UnderwritingResponse response = mock(UnderwritingResponse.class);
        when(response.applicationId()).thenReturn(101L);

        when(underwritingService.getPendingApplications()).thenReturn(Flux.just(response));
        when(underwritingService.getAllCases()).thenReturn(Flux.just(response));
        when(underwritingService.getMyCases(underwriter)).thenReturn(Flux.just(response));

        StepVerifier.create(controller.getPendingApplications())
                .assertNext(r -> assertThat(r.applicationId()).isEqualTo(101L))
                .verifyComplete();

        StepVerifier.create(controller.getAllCases())
                .assertNext(r -> assertThat(r.applicationId()).isEqualTo(101L))
                .verifyComplete();

        StepVerifier.create(controller.getMyCases(auth))
                .assertNext(r -> assertThat(r.applicationId()).isEqualTo(101L))
                .verifyComplete();
    }

    @Test
    void shouldHandleReviewAndDecisions() {
        UnderwritingResponse response = mock(UnderwritingResponse.class);
        when(response.applicationId()).thenReturn(101L);
        when(response.decision()).thenReturn("APPROVED");

        DecisionRequest dec = new DecisionRequest("Approved based on low risk score");
        when(underwritingService.startReview(underwriter, 101L)).thenReturn(Mono.just(response));
        when(underwritingService.approve(underwriter, 101L, dec)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.startReview(auth, 101L))
                .assertNext(entity -> assertThat(entity.getBody().applicationId()).isEqualTo(101L))
                .verifyComplete();

        StepVerifier.create(controller.approve(auth, 101L, dec))
                .assertNext(entity -> assertThat(entity.getBody().decision()).isEqualTo("APPROVED"))
                .verifyComplete();
    }
}
