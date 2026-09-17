package com.rgoncalo.financialapp.rest;

import com.rgoncalo.financialapp.application.security.AccessDeniedException;
import com.rgoncalo.financialapp.application.security.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converts application failures into stable HTTP error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> authenticationFailed(
            AuthenticationException exception
    ) {
        return error(
                HttpStatus.UNAUTHORIZED,
                "invalid_credentials",
                exception.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDenied(
            AccessDeniedException exception
    ) {
        return error(
                HttpStatus.FORBIDDEN,
                "access_denied",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidRequest(
            IllegalArgumentException exception
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "invalid_request",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidRequestBody(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request body is invalid.");

        return error(HttpStatus.BAD_REQUEST, "invalid_request", message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> endpointNotFound(
            NoResourceFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, "not_found", "Endpoint does not exist.");
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String code,
            String message
    ) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
