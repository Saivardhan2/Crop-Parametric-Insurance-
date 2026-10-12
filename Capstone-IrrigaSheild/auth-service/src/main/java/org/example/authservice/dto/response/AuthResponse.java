package org.example.authservice.dto.response;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        UserResponse user
) {
}
