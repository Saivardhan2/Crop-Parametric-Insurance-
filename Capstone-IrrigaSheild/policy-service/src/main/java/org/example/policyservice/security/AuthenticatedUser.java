package org.example.policyservice.security;

import java.security.Principal;

public record AuthenticatedUser(
        Long userId,
        String email
) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}