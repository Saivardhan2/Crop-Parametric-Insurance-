package org.example.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.authservice.exception.ApiErrorResponse;
import org.example.authservice.security.JwtAuthenticationFilter;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/actuator/health",
                                "/actuator/info"
                        )
                        .permitAll()

                        .pathMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .anyExchange()
                        .authenticated()
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(
                                        (exchange, exception) ->
                                                writeSecurityError(
                                                        exchange,
                                                        HttpStatus.UNAUTHORIZED,
                                                        "UNAUTHORIZED",
                                                        "Authentication is required"
                                                )
                                )
                                .accessDeniedHandler(
                                        (exchange, exception) ->
                                                writeSecurityError(
                                                        exchange,
                                                        HttpStatus.FORBIDDEN,
                                                        "ACCESS_DENIED",
                                                        "You do not have permission to access this resource"
                                                )
                                )
                )
                .addFilterAt(
                        jwtAuthenticationFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private Mono<Void> writeSecurityError(
            ServerWebExchange exchange,
            HttpStatus status,
            String errorCode,
            String message) {

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        errorCode,
                        message,
                        exchange.getRequest()
                                .getPath()
                                .value(),
                        null
                );

        byte[] responseBytes;

        try {
            responseBytes =
                    objectMapper.writeValueAsBytes(errorResponse);

        } catch (Exception exception) {
            responseBytes = message.getBytes();
        }

        exchange.getResponse().setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        DataBuffer dataBuffer = exchange
                .getResponse()
                .bufferFactory()
                .wrap(responseBytes);

        return exchange.getResponse()
                .writeWith(Mono.just(dataBuffer));
    }
}