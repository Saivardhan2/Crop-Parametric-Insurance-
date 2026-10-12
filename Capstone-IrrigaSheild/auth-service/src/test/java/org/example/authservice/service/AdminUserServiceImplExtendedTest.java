package org.example.authservice.service;

import java.time.LocalDateTime;

import org.example.authservice.dto.request.CreateUserRequest;
import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.entity.User;
import org.example.authservice.enums.Role;
import org.example.authservice.exception.ApiException;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplExtendedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    @DisplayName("createInternalUser should reject FARMER role in admin creation")
    void testCreateUserRejectsFarmerRole() {
        CreateUserRequest req = new CreateUserRequest("Farmer Joe", "farmer@test.com", "Password123", Role.FARMER);

        StepVerifier.create(authService.createInternalUser(req))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getErrorCode().equals("INVALID_INTERNAL_ROLE"))
                .verify();
    }

    @Test
    @DisplayName("createInternalUser should reject duplicate email with 409 Conflict")
    void testCreateUserDuplicateEmail() {
        CreateUserRequest req = new CreateUserRequest("Underwriter One", "uw@irrigashield.com", "Password123", Role.UNDERWRITER);
        when(userRepository.existsByEmailIgnoreCase("uw@irrigashield.com")).thenReturn(Mono.just(true));

        StepVerifier.create(authService.createInternalUser(req))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.CONFLICT
                        && ((ApiException) ex).getErrorCode().equals("EMAIL_ALREADY_EXISTS"))
                .verify();
    }

    @Test
    @DisplayName("createInternalUser should succeed for valid internal roles like UNDERWRITER")
    void testCreateUserSuccess() {
        CreateUserRequest req = new CreateUserRequest("Underwriter One", "uw@irrigashield.com", "Password123", Role.UNDERWRITER);
        when(userRepository.existsByEmailIgnoreCase("uw@irrigashield.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword123");

        User saved = User.builder()
                .userId(10L)
                .name("Underwriter One")
                .email("uw@irrigashield.com")
                .passwordHash("hashedPassword123")
                .role("UNDERWRITER")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(authService.createInternalUser(req))
                .assertNext(res -> {
                    assertThat(res.userId()).isEqualTo(10L);
                    assertThat(res.role()).isEqualTo("UNDERWRITER");
                    assertThat(res.status()).isEqualTo("ACTIVE");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateUserStatus should update user status and return updated UserResponse")
    void testUpdateUserStatusSuccess() {
        User existing = User.builder()
                .userId(5L)
                .name("Active User")
                .email("active@test.com")
                .passwordHash("hash")
                .role("FARMER")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(5L)).thenReturn(Mono.just(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(authService.updateUserStatus(5L, "INACTIVE"))
                .assertNext(res -> {
                    assertThat(res.userId()).isEqualTo(5L);
                    assertThat(res.status()).isEqualTo("INACTIVE");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateUserStatus should return 404 when user is not found")
    void testUpdateUserStatusNotFound() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(authService.updateUserStatus(99L, "INACTIVE"))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    @DisplayName("getAllUsers should stream all registered users")
    void testGetAllUsers() {
        User u1 = User.builder().userId(1L).name("User One").email("u1@test.com").role("FARMER").status("ACTIVE").build();
        User u2 = User.builder().userId(2L).name("User Two").email("u2@test.com").role("ADMIN").status("ACTIVE").build();

        when(userRepository.findAll()).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(authService.getAllUsers())
                .expectNextCount(2)
                .verifyComplete();
    }
}
