package com.r2s.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        ApiError error = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(error, status);
    }

    // [!] Handle errors Validate input data (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> errors = new HashMap<>();


        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    errors.put(fieldName, error.getDefaultMessage());
        });

        log.warn("[{}] Validation failed for {}: {} -> Path: {}",
                ex.getStatusCode(),
                req.getMethod(),
                errors,
                req.getRequestURI());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                req.getRequestURI(),
                errors);
    }

    // [!] Authenticated 401: Not logged in or wrong token (Bridging from Filter to)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Please log in to continue", req.getRequestURI(), null);
    }

    // [!] Access Denied 403: Insufficient permissions error (Bridging from Filter or @PreAuthorize)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", req.getRequestURI(), null);
    }

    // [!] Base Exception (401, 403, 404, 409...)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBusiness(BaseException ex, HttpServletRequest req) {

        log.warn("[{}] Business logic error: {} -> Path: {}",
                ex.getStatus(),
                ex.getMessage(),
                req.getRequestURI());

        return buildResponse(
                ex.getStatus(),
                ex.getMessage(),
                req.getRequestURI(),
                null);
    }


    // [!] 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex,HttpServletRequest req) {

        log.error("Unhandled exception: ", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal server error occurred",
                req.getRequestURI(),
                null);
    }
}