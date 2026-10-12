package org.example.eventclaimsservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class EventClaimsExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleApiException should map status, errorCode, and message")
    void testHandleApiException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/readings").build());
        ApiException ex = new ApiException(HttpStatus.CONFLICT, "DUPLICATE_READING", "Duplicate reading detected");

        ResponseEntity<ApiErrorResponse> response = handler.handleApiException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("DUPLICATE_READING");
        assertThat(response.getBody().message()).isEqualTo("Duplicate reading detected");
    }

    @Test
    @DisplayName("handleValidation should map validation errors")
    void testHandleValidation() throws NoSuchMethodException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/readings").build());
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "readingRequest");
        bindingResult.addError(new FieldError("readingRequest", "availabilityPercentage", "Must be <= 100"));

        MethodParameter param = new MethodParameter(EventClaimsExceptionHandlerTest.class.getDeclaredMethod("testHandleValidation"), -1);
        WebExchangeBindException bindEx = new WebExchangeBindException(param, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(bindEx, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().validationErrors()).containsEntry("availabilityPercentage", "Must be <= 100");
    }

    @Test
    @DisplayName("handleAccessDenied should return 403 Forbidden")
    void testHandleAccessDenied() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/readings").build());
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ApiErrorResponse> response = handler.handleAccessDenied(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("handleAuthentication should return 401 Unauthorized")
    void testHandleAuthentication() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/claims/my-claims").build());
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiErrorResponse> response = handler.handleAuthentication(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("handleResponseStatusException should return matching status")
    void testHandleResponseStatusException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/claims/1").build());
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("handleIllegalArgumentException should return 400 Bad Request")
    void testHandleIllegalArgumentException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/readings").build());
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgumentException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
    }
}
