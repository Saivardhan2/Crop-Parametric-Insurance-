package org.example.policyservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleAccessDenied should return 403 Forbidden")
    void testHandleAccessDenied() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/policies").build());
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ApiErrorResponse> response = handler.handleAccessDenied(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("handleAuthenticationRequired should return 401 Unauthorized")
    void testHandleAuthenticationRequired() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/policies/my-policies").build());
        AuthenticationCredentialsNotFoundException ex = new AuthenticationCredentialsNotFoundException("Not authenticated");

        ResponseEntity<ApiErrorResponse> response = handler.handleAuthenticationRequired(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("handleApiException should return exact status and error details")
    void testHandleApiException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/policies/1").build());
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND", "Policy not found");

        ResponseEntity<ApiErrorResponse> response = handler.handleApiException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("POLICY_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Policy not found");
    }

    @Test
    @DisplayName("handleValidation should return 400 with validation errors")
    void testHandleValidation() throws NoSuchMethodException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/policies/apply").build());
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "policyApplicationRequest");
        bindingResult.addError(new FieldError("policyApplicationRequest", "requestedCoverage", "Coverage must be greater than zero"));

        MethodParameter param = new MethodParameter(PolicyExceptionHandlerTest.class.getDeclaredMethod("testHandleValidation"), -1);
        WebExchangeBindException bindEx = new WebExchangeBindException(param, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(bindEx, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().validationErrors()).containsEntry("requestedCoverage", "Coverage must be greater than zero");
    }

    @Test
    @DisplayName("handleResponseStatusException should return matching status")
    void testHandleResponseStatusException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/plans/1").build());
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan ID");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("handleIllegalArgumentException should return 400 Bad Request")
    void testHandleIllegalArgumentException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/policies/1").build());
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgumentException(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("handleUnexpected should return 500 Internal Server Error")
    void testHandleUnexpected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/policies/1").build());
        RuntimeException ex = new RuntimeException("Unexpected runtime error");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpected(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }
}
