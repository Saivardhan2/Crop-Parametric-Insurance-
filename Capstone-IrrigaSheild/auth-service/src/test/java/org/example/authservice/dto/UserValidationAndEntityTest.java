package org.example.authservice.dto;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.authservice.dto.request.CreateUserRequest;
import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.request.RegisterRequest;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.dto.response.LoginResult;
import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.entity.User;
import org.example.authservice.enums.Role;
import org.example.authservice.enums.UserStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationAndEntityTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("RegisterRequest validation succeeds on valid data")
    void testRegisterRequestValid() {
        RegisterRequest req = new RegisterRequest("Jane Doe", "jane@example.com", "SecretPass123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("RegisterRequest validation fails on blank name")
    void testRegisterRequestBlankName() {
        RegisterRequest req = new RegisterRequest(" ", "jane@example.com", "SecretPass123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("RegisterRequest validation fails on invalid email")
    void testRegisterRequestInvalidEmail() {
        RegisterRequest req = new RegisterRequest("Jane Doe", "invalid-email-address", "SecretPass123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("RegisterRequest validation fails on password without number or uppercase")
    void testRegisterRequestWeakPassword() {
        RegisterRequest noNumber = new RegisterRequest("Jane Doe", "jane@example.com", "SecretPassWord");
        RegisterRequest noUpper = new RegisterRequest("Jane Doe", "jane@example.com", "secretpass123");
        RegisterRequest tooShort = new RegisterRequest("Jane Doe", "jane@example.com", "Pass1");

        assertThat(validator.validate(noNumber)).isNotEmpty();
        assertThat(validator.validate(noUpper)).isNotEmpty();
        assertThat(validator.validate(tooShort)).isNotEmpty();
    }

    @Test
    @DisplayName("LoginRequest validation succeeds on valid credentials")
    void testLoginRequestValid() {
        LoginRequest req = new LoginRequest("test@example.com", "Secret123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("LoginRequest validation fails on blank email or password")
    void testLoginRequestBlank() {
        LoginRequest blankEmail = new LoginRequest(" ", "Secret123");
        LoginRequest blankPass = new LoginRequest("test@example.com", " ");

        assertThat(validator.validate(blankEmail)).isNotEmpty();
        assertThat(validator.validate(blankPass)).isNotEmpty();
    }

    @Test
    @DisplayName("CreateUserRequest validation succeeds on valid admin request")
    void testCreateUserRequestValid() {
        CreateUserRequest req = new CreateUserRequest("Underwriter User", "uw@irrigashield.com", "SecretPass123", Role.UNDERWRITER);
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateUserRequest validation fails on null role")
    void testCreateUserRequestNullRole() {
        CreateUserRequest req = new CreateUserRequest("Admin User", "adm@irrigashield.com", "SecretPass123", null);
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }

    @Test
    @DisplayName("User entity builder and getters/setters should work correctly")
    void testUserEntityBuilderAndMutators() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .userId(1L)
                .name("Farmer Joe")
                .email("joe@farm.com")
                .passwordHash("hashed")
                .role("FARMER")
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user.getUserId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Farmer Joe");
        assertThat(user.getEmail()).isEqualTo("joe@farm.com");
        assertThat(user.getRole()).isEqualTo("FARMER");
        assertThat(user.getStatus()).isEqualTo("ACTIVE");
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);

        user.setStatus("INACTIVE");
        assertThat(user.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Enums and DTO response records should hold expected values")
    void testEnumsAndRecords() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.valueOf("FARMER")).isEqualTo(Role.FARMER);
        assertThat(Role.valueOf("UNDERWRITER")).isEqualTo(Role.UNDERWRITER);
        assertThat(Role.valueOf("DATA_PROVIDER")).isEqualTo(Role.DATA_PROVIDER);

        assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
        assertThat(UserStatus.valueOf("INACTIVE")).isEqualTo(UserStatus.INACTIVE);

        UserResponse userRes = new UserResponse(10L, "Alice", "alice@test.com", "FARMER", "ACTIVE");
        assertThat(userRes.userId()).isEqualTo(10L);
        assertThat(userRes.name()).isEqualTo("Alice");

        AuthResponse authRes = new AuthResponse("Bearer", "token123", 3600L, userRes);
        assertThat(authRes.accessToken()).isEqualTo("token123");
        assertThat(authRes.user()).isEqualTo(userRes);

        LoginResult loginResult = new LoginResult("token123", 3600L, userRes);
        assertThat(loginResult.accessToken()).isEqualTo("token123");
        assertThat(loginResult.expiresIn()).isEqualTo(3600L);
        assertThat(loginResult.user()).isEqualTo(userRes);
    }
}
