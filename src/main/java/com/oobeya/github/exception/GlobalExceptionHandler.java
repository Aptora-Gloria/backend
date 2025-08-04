package com.oobeya.github.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<?> handleGitHubApiException(GitHubApiException ex) {
        return new ResponseEntity<>(createErrorBody("GitHub API Error", ex.getMessage()), HttpStatus.BAD_GATEWAY);
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
