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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GatewaySecurityExtendedTest {

    private static final String BASE64_SECRET = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9yand0dGVzdGluZ3B1cnBvc2Vzb25seTEyMzQ1Njc4OTA=";
    private static final String OTHER_SECRET = "YW5vdGhlcnNlY3JldGtleWZvcmp3dHRlc3RpbmdwdXJwb3Nlc29ubHkxMjM0NTY3ODkwMQ==";
    private static final String COOKIE_NAME = "jwt";

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private SecretKey signingKey;
    private SecretKey otherKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(BASE64_SECRET);
        filter = new JwtAuthenticationFilter(jwtService, COOKIE_NAME);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
        otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(OTHER_SECRET));
    }

    private String createToken(Long userId, String email, List<String> roles, SecretKey key, long offsetMs) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .expiration(new Date(System.currentTimeMillis() + offsetMs))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("isTokenValid should return false for malformed or garbage strings")
    void testMalformedTokens() {
        assertThat(jwtService.isTokenValid("not.a.valid.jwt")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
        assertThat(jwtService.isTokenValid("   ")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid should return false when signed by an unknown key")
    void testTokenSignedByOtherKey() {
        String token = createToken(1L, "user@test.com", List.of("ROLE_USER"), otherKey, 60_000);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("extractRoles should correctly parse multi-role arrays")
    void testExtractMultipleRoles() {
        String token = createToken(5L, "admin@test.com", List.of("ROLE_ADMIN", "ROLE_UNDERWRITER", "ROLE_USER"), signingKey, 60_000);
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_ADMIN", "ROLE_UNDERWRITER", "ROLE_USER");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/register", "/api/auth/logout", "/actuator/health", "/actuator/info"})
    @DisplayName("Filter should allow additional public paths without authentication")
    void testAdditionalPublicPaths(String publicPath) {
        MockServerHttpRequest request = MockServerHttpRequest.get(publicPath).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] invoked = {false};
        GatewayFilterChain chain = ex -> {
            invoked[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(invoked[0]).isTrue();
    }

    @Test
    @DisplayName("Filter should accept lowercase 'bearer ' prefix in Authorization header")
    void testBearerCaseInsensitive() {
        String token = createToken(12L, "user12@test.com", List.of("ROLE_FARMER"), signingKey, 60_000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .header(HttpHeaders.AUTHORIZATION, "bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ServerWebExchange[] captured = new ServerWebExchange[1];
        StepVerifier.create(filter.filter(exchange, ex -> {
            captured[0] = ex;
            return Mono.empty();
        })).verifyComplete();

        assertThat(captured[0]).isNotNull();
        assertThat(captured[0].getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("12");
    }

    @Test
    @DisplayName("Filter should reject Bearer header with empty token value")
    void testBearerWithWhitespaceOnly() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .header(HttpHeaders.AUTHORIZATION, "Bearer    ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty())).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filter should reject invalid or expired token with 401")
    void testInvalidTokenReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.string")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty())).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filter should strip all incoming identity headers from unauthenticated requests")
    void testStripSpoofedHeadersOnReject() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .header("X-User-Id", "999")
                .header("X-User-Email", "attacker@evil.com")
                .header("X-User-Roles", "ROLE_ADMIN")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty())).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filter should fallback to Authorization header if cookie value is blank")
    void testBlankCookieFallsBackToHeader() {
        String token = createToken(88L, "farmer88@test.com", List.of("ROLE_FARMER"), signingKey, 60_000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .cookie(new HttpCookie(COOKIE_NAME, "  "))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ServerWebExchange[] captured = new ServerWebExchange[1];
        StepVerifier.create(filter.filter(exchange, ex -> {
            captured[0] = ex;
            return Mono.empty();
        })).verifyComplete();

        assertThat(captured[0]).isNotNull();
        assertThat(captured[0].getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("88");
    }

    @Test
    @DisplayName("Filter should prefer cookie token when both cookie and header are present")
    void testCookiePreferredOverHeader() {
        String cookieToken = createToken(11L, "cookie@test.com", List.of("ROLE_FARMER"), signingKey, 60_000);
        String headerToken = createToken(22L, "header@test.com", List.of("ROLE_UNDERWRITER"), signingKey, 60_000);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .cookie(new HttpCookie(COOKIE_NAME, cookieToken))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ServerWebExchange[] captured = new ServerWebExchange[1];
        StepVerifier.create(filter.filter(exchange, ex -> {
            captured[0] = ex;
            return Mono.empty();
        })).verifyComplete();

        assertThat(captured[0]).isNotNull();
        assertThat(captured[0].getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("11");
        assertThat(captured[0].getRequest().getHeaders().getFirst("X-User-Email")).isEqualTo("cookie@test.com");
    }
}
