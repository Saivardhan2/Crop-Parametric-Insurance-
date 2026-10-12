package org.example.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.example.apigateway.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private final JwtService jwtService;
    private final String cookieName;

    public JwtAuthenticationFilter(JwtService jwtService, @Value("${security.cookie.name}")
            String cookieName) {
        this.jwtService = jwtService;
        this.cookieName = cookieName;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        ServerHttpRequest sanitizedRequest =
                removeIncomingIdentityHeaders(exchange.getRequest());

        /*
         * CORS preflight requests do not contain the JWT cookie.
         * Allow OPTIONS requests to continue without authentication.
         */
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            ServerWebExchange preflightExchange = exchange.mutate()
                    .request(sanitizedRequest)
                    .build();

            return chain.filter(preflightExchange);
        }

        String path = sanitizedRequest.getURI().getPath();

        /*
         * Login, registration, logout, and actuator endpoints
         * do not require an existing JWT.
         */
        if (isPublicPath(path)) {
            ServerWebExchange publicExchange = exchange.mutate()
                    .request(sanitizedRequest)
                    .build();

            return chain.filter(publicExchange);
        }

        String token = null;
        HttpCookie jwtCookie = sanitizedRequest
                .getCookies()
                .getFirst(cookieName);

        if (jwtCookie != null && !jwtCookie.getValue().isBlank()) {
            token = jwtCookie.getValue();
        } else {
            String authHeader = sanitizedRequest.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = authHeader.substring(7).trim();
            }
        }

        if (token == null || token.isBlank()) {
            return writeUnauthorizedResponse(
                    exchange,
                    "Authentication credentials (cookie or Bearer token) are missing"
            );
        }

        if (!jwtService.isTokenValid(token)) {
            return writeUnauthorizedResponse(
                    exchange,
                    "Authentication token is invalid or expired"
            );
        }

        Long userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);
        List<String> roles = jwtService.extractRoles(token);

        ServerHttpRequest authenticatedRequest = sanitizedRequest
                .mutate()
                .header(USER_ID_HEADER, userId.toString())
                .header(USER_EMAIL_HEADER, email)
                .header(USER_ROLES_HEADER, String.join(",", roles))
                .build();

        ServerWebExchange authenticatedExchange = exchange.mutate()
                .request(authenticatedRequest)
                .build();

        return chain.filter(authenticatedExchange);
    }

    private ServerHttpRequest removeIncomingIdentityHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_EMAIL_HEADER);
                    headers.remove(USER_ROLES_HEADER);
                })
                .build();
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/logout")
                || path.startsWith("/actuator");
    }

    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized gateway request path={} reason={}", exchange.getRequest().getURI().getPath(), message);
        String responseBody = """
                {
                  "status": 401,
                  "errorCode": "UNAUTHORIZED",
                  "message": "%s",
                  "path": "%s"
                }
                """.formatted(message, exchange.getRequest()
                        .getURI()
                        .getPath()
        );

        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        String origin = exchange.getRequest().getHeaders().getOrigin();
        if (origin != null && !exchange.getResponse().getHeaders().containsKey("Access-Control-Allow-Origin")) {
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", origin);
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");
        }

        DataBuffer buffer = exchange
                .getResponse()
                .bufferFactory()
                .wrap(responseBytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}