package com.optimetric.github.infrastructure.http;

import com.optimetric.github.application.client.PullRequestClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestDetailResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestReviewResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestCommitResponse;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.shared.exception.GitHubApiException;
import com.optimetric.shared.exception.GitHubRateLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestClientAdapter implements PullRequestClientPort {

    private final WebClient githubApiWebClient;

    @Override
    public List<GitHubPullRequestResponse> getPullRequests(String accessToken, String owner, String repo, int page) {
        log.debug("Fetching pull requests for {}/{} - page {}", owner, repo, page);
        
        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUESTS_ENDPOINT_TEMPLATE, owner, repo);
        
        try {
            return githubApiWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                            .queryParam("state", "all")
                            .queryParam("per_page", 100)
                            .queryParam("page", page)
                            .build())
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_FETCH_FAILED))
                    .bodyToFlux(GitHubPullRequestResponse.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("GitHub API error fetching pull requests for {}/{}: {} - {}", 
                     owner, repo, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw handleWebClientException(ex, GitHubConstants.ERROR_PULL_REQUEST_FETCH_FAILED);
        } catch (Exception ex) {
            log.error("Unexpected error fetching pull requests for {}/{}", owner, repo, ex);
            throw new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_FETCH_FAILED + ex.getMessage(), ex);
        }
    }

    @Override
    public GitHubPullRequestDetailResponse getPullRequestDetails(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching pull request details for {}/{} - PR #{}", owner, repo, prNumber);
        
        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUEST_DETAILS_ENDPOINT_TEMPLATE, owner, repo, prNumber);
        
        try {
            return githubApiWebClient
                    .get()
                    .uri(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_DETAILS_FETCH_FAILED))
                    .bodyToMono(GitHubPullRequestDetailResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("GitHub API error fetching pull request details for {}/{} PR #{}: {} - {}", 
                     owner, repo, prNumber, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw handleWebClientException(ex, GitHubConstants.ERROR_PULL_REQUEST_DETAILS_FETCH_FAILED);
        } catch (Exception ex) {
            log.error("Unexpected error fetching pull request details for {}/{} PR #{}", owner, repo, prNumber, ex);
            throw new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_DETAILS_FETCH_FAILED + ex.getMessage(), ex);
        }
    }

    @Override
    public List<GitHubPullRequestReviewResponse> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching pull request reviews for {}/{} - PR #{}", owner, repo, prNumber);
        
        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUEST_REVIEWS_ENDPOINT_TEMPLATE, owner, repo, prNumber);
        
        try {
            return githubApiWebClient
                    .get()
                    .uri(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_REVIEWS_FETCH_FAILED))
                    .bodyToFlux(GitHubPullRequestReviewResponse.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("GitHub API error fetching pull request reviews for {}/{} PR #{}: {} - {}", 
                     owner, repo, prNumber, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw handleWebClientException(ex, GitHubConstants.ERROR_PULL_REQUEST_REVIEWS_FETCH_FAILED);
        } catch (Exception ex) {
            log.error("Unexpected error fetching pull request reviews for {}/{} PR #{}", owner, repo, prNumber, ex);
            throw new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_REVIEWS_FETCH_FAILED + ex.getMessage(), ex);
        }
    }

    @Override
    public List<GitHubPullRequestCommitResponse> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching pull request commits for {}/{} - PR #{}", owner, repo, prNumber);
        
        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUEST_COMMITS_ENDPOINT_TEMPLATE, owner, repo, prNumber);
        
        try {
            return githubApiWebClient
                    .get()
                    .uri(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED))
                    .bodyToFlux(GitHubPullRequestCommitResponse.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("GitHub API error fetching pull request commits for {}/{} PR #{}: {} - {}", 
                     owner, repo, prNumber, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw handleWebClientException(ex, GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED);
        } catch (Exception ex) {
            log.error("Unexpected error fetching pull request commits for {}/{} PR #{}", owner, repo, prNumber, ex);
            throw new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED + ex.getMessage(), ex);
        }
    }

    /**
     * Handles error responses from GitHub API with proper error categorization.
     */
    private reactor.core.publisher.Mono<? extends Throwable> handleErrorResponse(
            org.springframework.web.reactive.function.client.ClientResponse response, 
            String baseErrorMessage) {
        
        return response.bodyToMono(String.class)
                .map(body -> {
                    HttpStatus status = HttpStatus.valueOf(response.statusCode().value());
                    
                    // Log the error details
                    log.error("GitHub API error: {} - {} - Response: {}", 
                             status.value(), status.getReasonPhrase(), body);
                    
                    // Handle rate limiting
                    if (status == HttpStatus.TOO_MANY_REQUESTS) {
                        return handleRateLimitError(response, body, baseErrorMessage);
                    }
                    
                    // Handle authentication errors
                    if (status == HttpStatus.UNAUTHORIZED) {
                        return new GitHubApiException(baseErrorMessage + "Authentication failed. Please check your access token.");
                    }
                    
                    // Handle forbidden errors
                    if (status == HttpStatus.FORBIDDEN) {
                        return new GitHubApiException(baseErrorMessage + "Access forbidden. You may not have permission to access this resource.");
                    }
                    
                    // Handle not found errors
                    if (status == HttpStatus.NOT_FOUND) {
                        return new GitHubApiException(baseErrorMessage + "Resource not found. The repository or pull request may not exist.");
                    }
                    
                    // Handle server errors
                    if (status.is5xxServerError()) {
                        return new GitHubApiException(baseErrorMessage + "GitHub server error. Please try again later.");
                    }
                    
                    // Generic error
                    return new GitHubApiException(baseErrorMessage + body);
                });
    }

    /**
     * Handles WebClient exceptions with proper error categorization.
     */
    private GitHubApiException handleWebClientException(WebClientResponseException ex, String baseErrorMessage) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        
        // Handle rate limiting
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return handleRateLimitException(ex, baseErrorMessage);
        }
        
        // Handle authentication errors
        if (status == HttpStatus.UNAUTHORIZED) {
            return new GitHubApiException(baseErrorMessage + "Authentication failed. Please check your access token.");
        }
        
        // Handle forbidden errors
        if (status == HttpStatus.FORBIDDEN) {
            return new GitHubApiException(baseErrorMessage + "Access forbidden. You may not have permission to access this resource.");
        }
        
        // Handle not found errors
        if (status == HttpStatus.NOT_FOUND) {
            return new GitHubApiException(baseErrorMessage + "Resource not found. The repository or pull request may not exist.");
        }
        
        // Handle server errors
        if (status.is5xxServerError()) {
            return new GitHubApiException(baseErrorMessage + "GitHub server error. Please try again later.");
        }
        
        // Generic error
        return new GitHubApiException(baseErrorMessage + ex.getResponseBodyAsString(), ex);
    }

    /**
     * Handles rate limit errors by extracting reset time and remaining requests.
     */
    private GitHubRateLimitException handleRateLimitError(
            org.springframework.web.reactive.function.client.ClientResponse response, 
            String body, 
            String baseErrorMessage) {
        
        // Extract rate limit information from headers
        OffsetDateTime resetTime = null;
        int remainingRequests = 0;
        
        try {
            String resetHeader = response.headers().header("X-RateLimit-Reset").stream().findFirst().orElse(null);
            if (resetHeader != null) {
                long resetTimestamp = Long.parseLong(resetHeader);
                resetTime = OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(resetTimestamp), 
                                                   java.time.ZoneOffset.UTC);
            }
            
            String remainingHeader = response.headers().header("X-RateLimit-Remaining").stream().findFirst().orElse("0");
            remainingRequests = Integer.parseInt(remainingHeader);
            
        } catch (Exception e) {
            log.warn("Failed to parse rate limit headers", e);
        }
        
        String message = baseErrorMessage + "Rate limit exceeded. ";
        if (resetTime != null) {
            message += "Rate limit resets at " + resetTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        
        return new GitHubRateLimitException(message, resetTime, remainingRequests);
    }

    /**
     * Handles rate limit exceptions from WebClientResponseException.
     */
    private GitHubRateLimitException handleRateLimitException(WebClientResponseException ex, String baseErrorMessage) {
        // Extract rate limit information from headers
        OffsetDateTime resetTime = null;
        int remainingRequests = 0;
        
        try {
            String resetHeader = ex.getHeaders().getFirst("X-RateLimit-Reset");
            if (resetHeader != null) {
                long resetTimestamp = Long.parseLong(resetHeader);
                resetTime = OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(resetTimestamp), 
                                                   java.time.ZoneOffset.UTC);
            }
            
            String remainingHeader = ex.getHeaders().getFirst("X-RateLimit-Remaining");
            if (remainingHeader != null) {
                remainingRequests = Integer.parseInt(remainingHeader);
            }
            
        } catch (Exception e) {
            log.warn("Failed to parse rate limit headers", e);
        }
        
        String message = baseErrorMessage + "Rate limit exceeded. ";
        if (resetTime != null) {
            message += "Rate limit resets at " + resetTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        
        return new GitHubRateLimitException(message, ex, resetTime, remainingRequests);
    }
}