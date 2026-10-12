package org.example.authservice.dto.response;

public record UserResponse(

        Long userId,
        String name,
        String email,
        String role,
        String status

) {
}