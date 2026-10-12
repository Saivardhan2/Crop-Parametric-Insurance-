package org.example.authservice.security;

import java.util.List;

import org.springframework.http.HttpCookie;
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
public class JwtAuthenticationFilter implements WebFilter {

    private static final String TOKEN_COOKIE =
            "IRRIGASHIELD_TOKEN";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        String token = null;
        HttpCookie cookie = exchange
                .getRequest()
                .getCookies()
                .getFirst(TOKEN_COOKIE);

        if (cookie != null && !cookie.getValue().isBlank()) {
            token = cookie.getValue();
        } else {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = authHeader.substring(7).trim();
            }
        }

        if (token == null || token.isBlank()) {
            return chain.filter(exchange);
        }

        if (!jwtService.isTokenValid(token)) {
            return chain.filter(exchange);
        }

        String email =
                jwtService.extractEmail(token);

        List<SimpleGrantedAuthority> authorities =
                jwtService.extractRoles(token)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        authorities
                );

        return chain.filter(exchange)
                .contextWrite(
                        withAuthentication(authentication)
                );
    }
}