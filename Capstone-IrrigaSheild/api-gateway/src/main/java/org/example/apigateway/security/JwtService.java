package org.example.apigateway.security;

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

    public JwtService(@Value("${security.jwt.secret}") String base64Secret) {
        byte[] decodedSecret = Decoders.BASE64.decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(decodedSecret);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        Number userId = extractClaims(token)
                .get("userId", Number.class);
        if (userId == null) {
            throw new JwtException("Token does not contain user ID");
        }
        return userId.longValue();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token)
                .get("roles", List.class)
                .stream()
                .map(String::valueOf)
                .toList();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration()
                    .getTime() > System.currentTimeMillis();
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}