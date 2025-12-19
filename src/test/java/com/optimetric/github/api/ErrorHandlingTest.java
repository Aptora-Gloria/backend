package com.optimetric.github.api;

import com.optimetric.github.api.request.GetPullRequestRequest;
import com.optimetric.github.api.request.GetPullRequestsRequest;
import com.optimetric.github.api.request.InitiatePullRequestSyncRequest;
import com.optimetric.shared.exception.InvalidRepositoryFormatException;
import com.optimetric.shared.exception.PullRequestNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for error handling and validation improvements in the Pull Request system.
 */
@SpringBootTest
class ErrorHandlingTest {

    @Autowired
    private Validator validator;

    @Test
    void testGetPullRequestsRequestValidation() {
        // Test valid request
        GetPullRequestsRequest validRequest = new GetPullRequestsRequest("owner/repo");
        Set<ConstraintViolation<GetPullRequestsRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");

        // Test null repository name
        GetPullRequestsRequest nullRequest = new GetPullRequestsRequest(null);
        violations = validator.validate(nullRequest);
        assertFalse(violations.isEmpty(), "Null repository name should cause validation error");

        // Test empty repository name
        GetPullRequestsRequest emptyRequest = new GetPullRequestsRequest("");
        violations = validator.validate(emptyRequest);
        assertFalse(violations.isEmpty(), "Empty repository name should cause validation error");

        // Test invalid repository format
        GetPullRequestsRequest invalidFormatRequest = new GetPullRequestsRequest("invalid-format");
        violations = validator.validate(invalidFormatRequest);
        assertFalse(violations.isEmpty(), "Invalid repository format should cause validation error");

        // Test repository name with special characters
        GetPullRequestsRequest specialCharsRequest = new GetPullRequestsRequest("owner@/repo!");
        violations = validator.validate(specialCharsRequest);
        assertFalse(violations.isEmpty(), "Repository name with invalid characters should cause validation error");
    }

    @Test
    void testGetPullRequestRequestValidation() {
        // Test valid request
        GetPullRequestRequest validRequest = new GetPullRequestRequest("owner/repo", 123);
        Set<ConstraintViolation<GetPullRequestRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");

        // Test invalid PR number
        GetPullRequestRequest invalidPrRequest = new GetPullRequestRequest("owner/repo", -1);
        violations = validator.validate(invalidPrRequest);
        assertFalse(violations.isEmpty(), "Negative PR number should cause validation error");

        // Test null PR number
        GetPullRequestRequest nullPrRequest = new GetPullRequestRequest("owner/repo", null);
        violations = validator.validate(nullPrRequest);
        assertFalse(violations.isEmpty(), "Null PR number should cause validation error");

        // Test zero PR number
        GetPullRequestRequest zeroPrRequest = new GetPullRequestRequest("owner/repo", 0);
        violations = validator.validate(zeroPrRequest);
        assertFalse(violations.isEmpty(), "Zero PR number should cause validation error");
    }

    @Test
    void testInitiatePullRequestSyncRequestValidation() {
        // Test valid request
        InitiatePullRequestSyncRequest validRequest = new InitiatePullRequestSyncRequest("owner/repo");
        Set<ConstraintViolation<InitiatePullRequestSyncRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");

        // Test invalid repository format
        InitiatePullRequestSyncRequest invalidRequest = new InitiatePullRequestSyncRequest("owner");
        violations = validator.validate(invalidRequest);
        assertFalse(violations.isEmpty(), "Invalid repository format should cause validation error");
    }

    @Test
    void testExceptionCreation() {
        // Test PullRequestNotFoundException
        PullRequestNotFoundException prException = new PullRequestNotFoundException("PR not found");
        assertEquals("PR not found", prException.getMessage());

        // Test InvalidRepositoryFormatException
        InvalidRepositoryFormatException formatException = new InvalidRepositoryFormatException("Invalid format");
        assertEquals("Invalid format", formatException.getMessage());

        // Test exception with cause
        RuntimeException cause = new RuntimeException("Root cause");
        PullRequestNotFoundException exceptionWithCause = new PullRequestNotFoundException("PR not found", cause);
        assertEquals("PR not found", exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }

    @Test
    void testRepositoryFormatValidation() {
        // Valid formats
        assertTrue("owner/repo".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertTrue("my-org/my-repo".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertTrue("user123/project.name".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertTrue("org_name/repo_name".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));

        // Invalid formats
        assertFalse("owner".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertFalse("owner/".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertFalse("/repo".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertFalse("owner/repo/extra".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertFalse("owner@/repo".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
        assertFalse("owner/repo!".matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$"));
    }
}