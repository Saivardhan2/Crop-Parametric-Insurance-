package org.example.authservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.entity.User;
import org.example.authservice.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceAndMapperTest {

    private JwtService jwtService;
    private final String secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, 3600000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken(1L, "user@test.com", "FARMER");
        assertThat(token).isNotBlank();

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_FARMER");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid.token.here")).isFalse();
    }

    @Test
    void shouldMapUserToResponse() {
        User user = User.builder()
                .userId(100L)
                .name("John Doe")
                .email("john@test.com")
                .role("FARMER")
                .status("ACTIVE")
                .build();

        UserResponse response = UserMapper.toResponse(user);
        assertThat(response.userId()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@test.com");
        assertThat(response.role()).isEqualTo("FARMER");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }
}
