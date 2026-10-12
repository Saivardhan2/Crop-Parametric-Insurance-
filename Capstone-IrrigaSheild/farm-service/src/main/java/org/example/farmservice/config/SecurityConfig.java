package org.example.farmservice.config;

import org.example.farmservice.security
        .TrustedHeaderAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation
        .method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation
        .web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server
        .SecurityWebFiltersOrder;
import org.springframework.security.config.web.server
        .ServerHttpSecurity;
import org.springframework.security.web.server
        .SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final TrustedHeaderAuthenticationFilter
            trustedHeaderFilter;

    public SecurityConfig(
            TrustedHeaderAuthenticationFilter
                    trustedHeaderFilter) {

        this.trustedHeaderFilter =
                trustedHeaderFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(
                        ServerHttpSecurity
                                .FormLoginSpec::disable
                )
                .httpBasic(
                        ServerHttpSecurity
                                .HttpBasicSpec::disable
                )
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        )
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .addFilterAt(
                        trustedHeaderFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }
}