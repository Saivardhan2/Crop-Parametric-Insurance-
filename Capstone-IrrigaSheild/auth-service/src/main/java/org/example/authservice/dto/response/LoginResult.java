package org.example.authservice.dto.response;

public record LoginResult(
        String accessToken,
        long expiresIn,
        UserResponse user
) {
}