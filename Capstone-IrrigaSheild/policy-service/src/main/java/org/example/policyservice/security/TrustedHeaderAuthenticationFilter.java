package org.example.policyservice.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

import static org.springframework.security.core.context
        .ReactiveSecurityContextHolder.withAuthentication;

@Component
public class TrustedHeaderAuthenticationFilter
        implements WebFilter {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        String userIdHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id");

        String emailHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Email");

        String rolesHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Roles");

        if (userIdHeader == null
                || emailHeader == null
                || rolesHeader == null) {

            return chain.filter(exchange);
        }

        Long userId;

        try {
            userId = Long.valueOf(userIdHeader);
        } catch (NumberFormatException exception) {
            return chain.filter(exchange);
        }

        List<SimpleGrantedAuthority> authorities =
                Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        AuthenticatedUser principal =
                new AuthenticatedUser(
                        userId,
                        emailHeader
                );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        return chain.filter(exchange)
                .contextWrite(
                        withAuthentication(authentication)
                );
    }
}