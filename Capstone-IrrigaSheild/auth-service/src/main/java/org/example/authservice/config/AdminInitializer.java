package org.example.authservice.config;

import java.time.LocalDateTime;
import java.util.Locale;
import org.example.authservice.entity.User;
import org.example.authservice.enums.Role;
import org.example.authservice.enums.UserStatus;
import org.example.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${application.admin.name}")
            String adminName,
            @Value("${application.admin.email}")
            String adminEmail,
            @Value("${application.admin.password}")
            String adminPassword) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {

        String normalizedEmail = adminEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        userRepository
                .existsByEmailIgnoreCase(normalizedEmail)
                .flatMap(exists -> {

                    if (exists) {
                        log.info("Initial admin account already exists");
                        return Mono.empty();
                    }

                    return Mono.fromCallable(
                                    () -> passwordEncoder.encode(adminPassword)
                            )
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(encodedPassword -> {

                                LocalDateTime now = LocalDateTime.now();

                                return User.builder()
                                        .name(adminName.trim())
                                        .email(normalizedEmail)
                                        .passwordHash(encodedPassword)
                                        .role(Role.ADMIN.name())
                                        .status(UserStatus.ACTIVE.name())
                                        .createdAt(now)
                                        .updatedAt(now)
                                        .build();
                            })
                            .flatMap(userRepository::save)
                            .doOnSuccess(user ->
                                    log.info(
                                            "Initial admin account created"
                                    )
                            );
                })
                .doOnError(error ->
                        log.error(
                                "Unable to initialize admin account",
                                error
                        )
                )
                .subscribe();
    }
}