package org.example.authservice.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.request.RegisterRequest;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.dto.response.LoginResult;
import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.entity.User;
import org.example.authservice.enums.Role;
import org.example.authservice.enums.UserStatus;
import org.example.authservice.exception.ApiException;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.JwtService;
import org.example.authservice.util.UserMapper;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.authservice.dto.request.CreateUserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
public class AuthServiceImpl implements AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<UserResponse> register(
            RegisterRequest request) {

        String normalizedEmail =
                normalizeEmail(request.email());

        return userRepository
                .existsByEmailIgnoreCase(normalizedEmail)
                .flatMap(emailExists -> {

                    if (emailExists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "EMAIL_ALREADY_EXISTS",
                                        "An account already exists with this email"
                                )
                        );
                    }

                    return encodePassword(request.password())
                            .map(encodedPassword ->
                                    createUser(
                                            request,
                                            normalizedEmail,
                                            encodedPassword
                                    )
                            )
                            .flatMap(userRepository::save)
                            .map(UserMapper::toResponse);
                });
    }

    @Override
    public Mono<LoginResult> login(LoginRequest request) {

        String normalizedEmail =
                normalizeEmail(request.email());

        return userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .switchIfEmpty(invalidCredentials())
                .flatMap(user ->
                        validateLogin(
                                request.password(),
                                user
                        )
                )
                .map(user -> {

                    String token = jwtService.generateToken(
                            user.getUserId(),
                            user.getEmail(),
                            user.getRole()
                    );

                    return new LoginResult(
                            token,
                            jwtService.getExpirationSeconds(),
                            UserMapper.toResponse(user)
                    );
                });
    }
    @Override
    public Mono<UserResponse> createInternalUser(
            CreateUserRequest request) {

        String normalizedEmail =
                normalizeEmail(request.email());

        if (request.role() == Role.FARMER) {
            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_INTERNAL_ROLE",
                            "Farmer accounts must use public registration"
                    )
            );
        }

        return userRepository
                .existsByEmailIgnoreCase(normalizedEmail)
                .flatMap(emailExists -> {

                    if (emailExists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "EMAIL_ALREADY_EXISTS",
                                        "An account already exists with this email"
                                )
                        );
                    }

                    return encodePassword(request.password())
                            .map(encodedPassword -> {

                                LocalDateTime now =
                                        LocalDateTime.now();

                                return User.builder()
                                        .name(request.name().trim())
                                        .email(normalizedEmail)
                                        .passwordHash(encodedPassword)
                                        .role(request.role().name())
                                        .status(UserStatus.ACTIVE.name())
                                        .createdAt(now)
                                        .updatedAt(now)
                                        .build();
                            })
                            .flatMap(userRepository::save)
                            .map(UserMapper::toResponse);
                });
    }

    @Override
    public Flux<UserResponse> getAllUsers() {

        return userRepository
                .findAll()
                .map(UserMapper::toResponse);
    }

    @Override
    public Mono<UserResponse> updateUserStatus(Long userId, String status) {
        return userRepository.findById(userId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "USER_NOT_FOUND",
                                        "User was not found"
                                )
                        )
                )
                .flatMap(user -> {
                    user.setStatus(status.toUpperCase(Locale.ROOT));
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .map(UserMapper::toResponse);
    }

    @Override
    public Mono<UserResponse> getCurrentUser(
            String email) {

        return userRepository
                .findByEmailIgnoreCase(
                        normalizeEmail(email)
                )
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "USER_NOT_FOUND",
                                        "User was not found"
                                )
                        )
                )
                .map(UserMapper::toResponse);
    }

    private User createUser(
            RegisterRequest request,
            String normalizedEmail,
            String encodedPassword) {

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .name(request.name().trim())
                .email(normalizedEmail)
                .passwordHash(encodedPassword)
                .role(Role.FARMER.name())
                .status(UserStatus.ACTIVE.name())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Mono<User> validateLogin(
            String rawPassword,
            User user) {

        if (!UserStatus.ACTIVE.name()
                .equals(user.getStatus())) {

            return invalidCredentials();
        }

        return Mono.fromCallable(() ->
                        passwordEncoder.matches(
                                rawPassword,
                                user.getPasswordHash()
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(passwordMatches -> {
                    if (!passwordMatches) {
                        return invalidCredentials();
                    }
                    return Mono.just(user);
                });
    }

    private Mono<String> encodePassword(
            String rawPassword) {

        return Mono.fromCallable(() ->
                        passwordEncoder.encode(rawPassword)
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    private <T> Mono<T> invalidCredentials() {

        return Mono.error(
                new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS",
                        "Invalid email or password"
                )
        );
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}