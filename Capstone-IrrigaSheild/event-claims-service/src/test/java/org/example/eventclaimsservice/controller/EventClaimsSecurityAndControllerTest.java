package org.example.eventclaimsservice.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.eventclaimsservice.dto.ClaimResponse;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.security.AuthenticatedUser;
import org.example.eventclaimsservice.security.TrustedHeaderAuthenticationFilter;
import org.example.eventclaimsservice.service.EventClaimsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventClaimsSecurityAndControllerTest {

    @Mock
    private EventClaimsService eventClaimsService;

    @InjectMocks
    private EventClaimsController controller;

    private final TrustedHeaderAuthenticationFilter filter = new TrustedHeaderAuthenticationFilter();

    @Test
    @DisplayName("Filter should populate reactive security context when trusted headers are present")
    void testFilterWithValidHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/claims/my-claims")
                .header("X-User-Id", "42")
                .header("X-User-Email", "farmer@irrigashield.com")
                .header("X-User-Roles", "ROLE_FARMER,ROLE_USER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = ex -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .doOnNext(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
                    AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
                    assertThat(user.userId()).isEqualTo(42L);
                    assertThat(user.email()).isEqualTo("farmer@irrigashield.com");
                    assertThat(auth.getAuthorities()).extracting("authority")
                            .containsExactlyInAnyOrder("ROLE_FARMER", "ROLE_USER");
                })
                .then();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Filter should pass through without error when headers are missing or invalid")
    void testFilterWithMissingOrInvalidHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/claims/my-claims").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        MockServerHttpRequest badRequest = MockServerHttpRequest.get("/api/claims/my-claims")
                .header("X-User-Id", "not-a-number")
                .header("X-User-Email", "test@irrigashield.com")
                .header("X-User-Roles", "ROLE_FARMER")
                .build();
        StepVerifier.create(filter.filter(MockServerWebExchange.from(badRequest), chain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Controller should submit reading and return 201 Created")
    void testSubmitReading() {
        AuthenticatedUser user = new AuthenticatedUser(10L, "provider@irrigashield.com");
        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());

        IrrigationReadingRequest req = new IrrigationReadingRequest(
                1L,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );

        ClaimResponse claimResponse = new ClaimResponse(
                100L,
                55L,
                1L,
                10L,
                new BigDecimal("25.00"),
                true,
                new BigDecimal("50.00"),
                new BigDecimal("5000.00"),
                "SUCCESS",
                "APPROVED",
                LocalDateTime.now()
        );

        when(eventClaimsService.processReading(eq(user), any(IrrigationReadingRequest.class)))
                .thenReturn(Mono.just(claimResponse));

        StepVerifier.create(controller.submitReading(auth, req))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().claimId()).isEqualTo(55L);
                    assertThat(response.getBody().compensationAmount()).isEqualByComparingTo("5000.00");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Controller should get claims for authenticated farmer")
    void testGetMyClaims() {
        AuthenticatedUser user = new AuthenticatedUser(20L, "farmer@irrigashield.com");
        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());

        ClaimResponse claim = new ClaimResponse(
                200L,
                99L,
                2L,
                20L,
                new BigDecimal("15.00"),
                true,
                new BigDecimal("80.00"),
                new BigDecimal("2500.00"),
                "SUCCESS",
                "APPROVED",
                LocalDateTime.now()
        );

        when(eventClaimsService.getMyClaims(eq(user)))
                .thenReturn(Flux.just(claim));

        StepVerifier.create(controller.getMyClaims(auth))
                .assertNext(res -> {
                    assertThat(res.claimId()).isEqualTo(99L);
                    assertThat(res.claimStatus()).isEqualTo("APPROVED");
                })
                .verifyComplete();
    }
}
