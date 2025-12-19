package com.optimetric.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<?> handleGitHubApiException(GitHubApiException ex) {
        return new ResponseEntity<>(createErrorBody("GitHub API Error", ex.getMessage()), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<?> handleRepositoryNotFoundException(RepositoryNotFoundException ex) {
        return new ResponseEntity<>(createErrorBody("Repository Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<?> handleBranchNotFoundException(BranchNotFoundException ex) {
        return new ResponseEntity<>(createErrorBody("Branch Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(createErrorBody("User Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PullRequestNotFoundException.class)
    public ResponseEntity<?> handlePullRequestNotFoundException(PullRequestNotFoundException ex) {
        return new ResponseEntity<>(createErrorBody("Pull Request Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SyncJobException.class)
    public ResponseEntity<?> handleSyncJobException(SyncJobException ex) {
        return new ResponseEntity<>(createErrorBody("Synchronization Error", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GitHubRateLimitException.class)
    public ResponseEntity<?> handleGitHubRateLimitException(GitHubRateLimitException ex) {
        Map<String, Object> errorBody = createErrorBody("GitHub Rate Limit Exceeded", ex.getMessage());
        errorBody.put("resetTime", ex.getResetTime());
        errorBody.put("remainingRequests", ex.getRemainingRequests());
        return new ResponseEntity<>(errorBody, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(InvalidRepositoryFormatException.class)
    public ResponseEntity<?> handleInvalidRepositoryFormatException(InvalidRepositoryFormatException ex) {
        return new ResponseEntity<>(createErrorBody("Invalid Repository Format", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(
                createErrorBody("Access Denied", "You do not have permission to access this resource. Please ensure you are authenticated with a valid JWT token."),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        return new ResponseEntity<>(
                createErrorBody("Authentication Required", "You must be authenticated to access this resource. Please provide a valid JWT token in the Authorization header."),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(createErrorBody("Validation Error", message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(createErrorBody("Validation Error", message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return new ResponseEntity<>(createErrorBody("Internal Server Error", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorBody(String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
