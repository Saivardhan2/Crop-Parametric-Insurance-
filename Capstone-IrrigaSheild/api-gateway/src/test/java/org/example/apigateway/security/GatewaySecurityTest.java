package org.example.apigateway.security;

import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.apigateway.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewaySecurityTest {

    private static final String BASE64_SECRET = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9yand0dGVzdGluZ3B1cnBvc2Vzb25seTEyMzQ1Njc4OTA=";
    private static final String COOKIE_NAME = "jwt";

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(BASE64_SECRET);
        filter = new JwtAuthenticationFilter(jwtService, COOKIE_NAME);
        byte[] decoded = Decoders.BASE64.decode(BASE64_SECRET);
        signingKey = Keys.hmacShaKeyFor(decoded);
    }

    private String createTestToken(Long userId, String email, List<String> roles, long expirationOffsetMs) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .expiration(new Date(System.currentTimeMillis() + expirationOffsetMs))
                .signWith(signingKey)
                .compact();
    }

    @Test
    @DisplayName("JwtService should extract claims and validate token correctly")
    void testJwtServiceClaimsAndValidation() {
        String token = createTestToken(100L, "farmer@irrigashield.com", List.of("ROLE_FARMER"), 60_000);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(100L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("farmer@irrigashield.com");
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_FARMER");
    }

    @Test
    @DisplayName("JwtService should reject expired token or missing userId")
    void testJwtServiceInvalidToken() {
        String expiredToken = createTestToken(100L, "farmer@irrigashield.com", List.of("ROLE_FARMER"), -10_000);
        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();

        String tokenWithoutUserId = Jwts.builder()
                .subject("test@irrigashield.com")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();
        assertThatThrownBy(() -> jwtService.extractUserId(tokenWithoutUserId))
                .hasMessageContaining("does not contain user ID");
    }

    @Test
    @DisplayName("Filter should allow OPTIONS preflight requests without auth")
    void testOptionsRequestPasses() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/farms").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] chainCalled = {false};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled[0]).isTrue();
    }

    @Test
    @DisplayName("Filter should allow public paths without token")
    void testPublicPathsPass() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] chainCalled = {false};
        GatewayFilterChain chain = ex -> {
            chainCalled[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled[0]).isTrue();
    }

    @Test
    @DisplayName("Filter should reject protected path when credentials are missing")
    void testProtectedPathMissingCredentials() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filter should accept valid Bearer token and inject user headers while stripping spoofed headers")
    void testValidBearerTokenAuth() {
        String token = createTestToken(50L, "officer@irrigashield.com", List.of("ROLE_UNDERWRITER"), 60_000);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/policies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-User-Id", "999")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ServerWebExchange[] interceptedExchange = new ServerWebExchange[1];
        GatewayFilterChain chain = ex -> {
            interceptedExchange[0] = ex;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(interceptedExchange[0]).isNotNull();
        HttpHeaders forwardedHeaders = interceptedExchange[0].getRequest().getHeaders();
        assertThat(forwardedHeaders.getFirst("X-User-Id")).isEqualTo("50");
        assertThat(forwardedHeaders.getFirst("X-User-Email")).isEqualTo("officer@irrigashield.com");
        assertThat(forwardedHeaders.getFirst("X-User-Roles")).isEqualTo("ROLE_UNDERWRITER");
    }

    @Test
    @DisplayName("Filter should accept valid JWT cookie and verify filter order")
    void testValidCookieAuthAndOrder() {
        String token = createTestToken(77L, "farmer@irrigashield.com", List.of("ROLE_FARMER"), 60_000);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .cookie(new HttpCookie(COOKIE_NAME, token))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ServerWebExchange[] interceptedExchange = new ServerWebExchange[1];
        GatewayFilterChain chain = ex -> {
            interceptedExchange[0] = ex;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(interceptedExchange[0]).isNotNull();
        HttpHeaders forwardedHeaders = interceptedExchange[0].getRequest().getHeaders();
        assertThat(forwardedHeaders.getFirst("X-User-Id")).isEqualTo("77");
        assertThat(forwardedHeaders.getFirst("X-User-Email")).isEqualTo("farmer@irrigashield.com");
        assertThat(filter.getOrder()).isEqualTo(-100);
    }
}
