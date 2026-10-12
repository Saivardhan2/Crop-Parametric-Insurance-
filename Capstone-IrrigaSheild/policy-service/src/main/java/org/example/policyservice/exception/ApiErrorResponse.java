package org.example.policyservice.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(

        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, String> validationErrors

) {
}