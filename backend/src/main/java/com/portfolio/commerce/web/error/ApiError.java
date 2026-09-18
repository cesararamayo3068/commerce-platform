package com.portfolio.commerce.web.error;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error response body returned by the REST API.
 *
 * For Bean Validation failures, {@code fieldErrors} contains one entry per
 * invalid field. Stack traces are never exposed to the client.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {
    }
}
