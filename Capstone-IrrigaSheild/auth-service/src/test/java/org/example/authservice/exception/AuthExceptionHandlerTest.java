package org.example.authservice.exception;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleApiException should return exact status and error details")
    void testHandleApiException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/profile").build());
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User does not exist");

        ResponseEntity<ApiErrorResponse> response = handler.handleApiException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("User does not exist");
        assertThat(response.getBody().path()).isEqualTo("/api/auth/profile");
    }

    @Test
    @DisplayName("handleValidationException should return 400 with field errors")
    void testHandleValidationException() throws NoSuchMethodException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/register").build());
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "email", "Email format is invalid"));

        MethodParameter parameter = new MethodParameter(
                AuthExceptionHandlerTest.class.getDeclaredMethod("testHandleValidationException"), -1
        );
        WebExchangeBindException bindEx = new WebExchangeBindException(parameter, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(bindEx, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().validationErrors()).containsEntry("email", "Email format is invalid");
    }

    @Test
    @DisplayName("handleDuplicateKeyException should return 409 Conflict")
    void testHandleDuplicateKeyException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/register").build());
        DuplicateKeyException ex = new DuplicateKeyException("Duplicate entry 'test@example.com' for key 'users.email'");

        ResponseEntity<ApiErrorResponse> response = handler.handleDuplicateKeyException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("DUPLICATE_RESOURCE");
    }

    @Test
    @DisplayName("handleResponseStatusException should return matching status and message")
    void testHandleResponseStatusException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/admin/users").build());
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Access is denied");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("FORBIDDEN");
        assertThat(response.getBody().message()).isEqualTo("Access is denied");
    }

    @Test
    @DisplayName("handleIllegalArgumentException should return 400 Bad Request")
    void testHandleIllegalArgumentException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/login").build());
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgumentException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("Invalid argument provided");
    }

    @Test
    @DisplayName("handleUnexpectedException should return 500 Internal Server Error")
    void testHandleUnexpectedException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/unknown").build());
        RuntimeException ex = new RuntimeException("Unexpected fatal error");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}
