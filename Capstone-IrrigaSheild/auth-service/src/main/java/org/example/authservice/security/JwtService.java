package org.example.authservice.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}")
            String base64Secret,

            @Value("${security.jwt.expiration-ms}")
            long expirationMs) {

        byte[] decodedSecret =
                Decoders.BASE64.decode(base64Secret);

        this.signingKey =
                Keys.hmacShaKeyFor(decodedSecret);

        this.expirationMs = expirationMs;
    }

    public String generateToken(
            Long userId,
            String email,
            String role) {

        Instant issuedAt = Instant.now();

        Instant expiresAt =
                issuedAt.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim(
                        "roles",
                        List.of("ROLE_" + role)
                )
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {

        return extractClaims(token)
                .get("roles", List.class);
    }

    public boolean isTokenValid(String token) {

        try {
            Claims claims = extractClaims(token);

            return claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
