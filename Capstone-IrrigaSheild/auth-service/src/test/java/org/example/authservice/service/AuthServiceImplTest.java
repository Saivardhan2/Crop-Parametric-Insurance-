package org.example.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.example.authservice.dto.request.CreateUserRequest;
import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.request.RegisterRequest;
import org.example.authservice.entity.User;
import org.example.authservice.enums.Role;
import org.example.authservice.exception.ApiException;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerShouldSucceedWhenEmailIsNew() {
        RegisterRequest request = new RegisterRequest("Farmer Joe", "joe@farm.com", "Password123");

        when(userRepository.existsByEmailIgnoreCase("joe@farm.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .userId(1L)
                .name("Farmer Joe")
                .email("joe@farm.com")
                .passwordHash("encodedPassword")
                .role("FARMER")
                .status("ACTIVE")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(authService.register(request))
                .assertNext(res -> {
                    assertThat(res.userId()).isEqualTo(1L);
                    assertThat(res.email()).isEqualTo("joe@farm.com");
                    assertThat(res.role()).isEqualTo("FARMER");
                })
                .verifyComplete();
    }

    @Test
    void registerShouldFailWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Farmer Joe", "joe@farm.com", "Password123");
        when(userRepository.existsByEmailIgnoreCase("joe@farm.com")).thenReturn(Mono.just(true));

        StepVerifier.create(authService.register(request))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void loginShouldSucceedWithValidCredentials() {
        LoginRequest request = new LoginRequest("joe@farm.com", "Password123");
        User user = User.builder()
                .userId(1L)
                .name("Farmer Joe")
                .email("joe@farm.com")
                .passwordHash("encodedPassword")
                .role("FARMER")
                .status("ACTIVE")
                .build();

        when(userRepository.findByEmailIgnoreCase("joe@farm.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("Password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(1L, "joe@farm.com", "FARMER")).thenReturn("jwt-token-123");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        StepVerifier.create(authService.login(request))
                .assertNext(res -> {
                    assertThat(res.accessToken()).isEqualTo("jwt-token-123");
                    assertThat(res.expiresIn()).isEqualTo(3600L);
                    assertThat(res.user().email()).isEqualTo("joe@farm.com");
                })
                .verifyComplete();
    }

    @Test
    void loginShouldFailWhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@farm.com", "Password123");
        when(userRepository.findByEmailIgnoreCase("unknown@farm.com")).thenReturn(Mono.empty());

        StepVerifier.create(authService.login(request))
                .expectErrorMatches(t -> t instanceof ApiException &&
                        ((ApiException) t).getStatus() == HttpStatus.UNAUTHORIZED)
                .verify();
    }

    @Test
    void loginShouldFailWhenPasswordIncorrect() {
        LoginRequest request = new LoginRequest("joe@farm.com", "WrongPassword");
        User user = User.builder()
                .userId(1L)
                .name("Farmer Joe")
                .email("joe@farm.com")
                .passwordHash("encodedPassword")
                .role("FARMER")
                .status("ACTIVE")
                .build();

        when(userRepository.findByEmailIgnoreCase("joe@farm.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        StepVerifier.create(authService.login(request))
                .expectErrorMatches(t -> t instanceof ApiException &&
                        ((ApiException) t).getStatus() == HttpStatus.UNAUTHORIZED)
                .verify();
    }

    @Test
    void loginShouldFailWhenAccountInactive() {
        LoginRequest request = new LoginRequest("joe@farm.com", "Password123");
        User user = User.builder()
                .userId(1L)
                .name("Farmer Joe")
                .email("joe@farm.com")
                .passwordHash("encodedPassword")
                .role("FARMER")
                .status("INACTIVE")
                .build();

        when(userRepository.findByEmailIgnoreCase("joe@farm.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("Password123", "encodedPassword")).thenReturn(true);

        StepVerifier.create(authService.login(request))
                .expectErrorMatches(t -> t instanceof ApiException &&
                        ((ApiException) t).getStatus() == HttpStatus.UNAUTHORIZED)
                .verify();
    }

    @Test
    void createInternalUserShouldRejectFarmerRole() {
        CreateUserRequest req = new CreateUserRequest("Farmer", "f@farm.com", "Pass1234", Role.FARMER);
        StepVerifier.create(authService.createInternalUser(req))
                .expectErrorMatches(t -> t instanceof ApiException &&
                        ((ApiException) t).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void createInternalUserShouldSucceedForUnderwriter() {
        CreateUserRequest req = new CreateUserRequest("UW One", "uw@farm.com", "Pass1234", Role.UNDERWRITER);
        when(userRepository.existsByEmailIgnoreCase("uw@farm.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("Pass1234")).thenReturn("encPass");

        User savedUser = User.builder()
                .userId(2L)
                .name("UW One")
                .email("uw@farm.com")
                .passwordHash("encPass")
                .role("UNDERWRITER")
                .status("ACTIVE")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(authService.createInternalUser(req))
                .assertNext(res -> {
                    assertThat(res.userId()).isEqualTo(2L);
                    assertThat(res.role()).isEqualTo("UNDERWRITER");
                })
                .verifyComplete();
    }

    @Test
    void getAllUsersShouldReturnFlux() {
        User u1 = User.builder().userId(1L).email("a@a.com").role("ADMIN").status("ACTIVE").build();
        User u2 = User.builder().userId(2L).email("b@b.com").role("FARMER").status("ACTIVE").build();
        when(userRepository.findAll()).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(authService.getAllUsers())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void updateUserStatusShouldUpdateAndSave() {
        User u1 = User.builder().userId(1L).email("a@a.com").role("ADMIN").status("ACTIVE").build();
        when(userRepository.findById(1L)).thenReturn(Mono.just(u1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(authService.updateUserStatus(1L, "INACTIVE"))
                .assertNext(res -> assertThat(res.status()).isEqualTo("INACTIVE"))
                .verifyComplete();
    }

    @Test
    void getCurrentUserShouldFindUser() {
        User u1 = User.builder().userId(1L).email("a@a.com").role("ADMIN").status("ACTIVE").build();
        when(userRepository.findByEmailIgnoreCase("a@a.com")).thenReturn(Mono.just(u1));

        StepVerifier.create(authService.getCurrentUser("a@a.com"))
                .assertNext(res -> assertThat(res.email()).isEqualTo("a@a.com"))
                .verifyComplete();
    }
}
