package com.optimetric.github.infrastructure.http.adapter;

import com.optimetric.github.application.dto.PullRequestCommitFromApi;
import com.optimetric.github.application.dto.PullRequestDetailFromApi;
import com.optimetric.github.application.dto.PullRequestFromApi;
import com.optimetric.github.application.dto.PullRequestReviewFromApi;
import com.optimetric.github.application.port.out.PullRequestClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.core.mapper.PullRequestFromApiMapper;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestCommitResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestDetailResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestReviewResponse;
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
    public List<PullRequestFromApi> getPullRequests(String accessToken, String owner, String repo, int page) {
        log.debug("Fetching pull requests for {}/{} - page {}", owner, repo, page);

        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUESTS_ENDPOINT_TEMPLATE, owner, repo);

        try {
            List<GitHubPullRequestResponse> raw = githubApiWebClient
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
            return PullRequestFromApiMapper.toPullRequestFromApiList(raw);
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
    public PullRequestDetailFromApi getPullRequestDetails(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching pull request details for {}/{} - PR #{}", owner, repo, prNumber);

        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUEST_DETAILS_ENDPOINT_TEMPLATE, owner, repo, prNumber);

        try {
            GitHubPullRequestDetailResponse raw = githubApiWebClient
                    .get()
                    .uri(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_DETAILS_FETCH_FAILED))
                    .bodyToMono(GitHubPullRequestDetailResponse.class)
                    .block();
            return PullRequestFromApiMapper.toPullRequestDetailFromApi(raw);
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
    public List<PullRequestReviewFromApi> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching pull request reviews for {}/{} - PR #{}", owner, repo, prNumber);

        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUEST_REVIEWS_ENDPOINT_TEMPLATE, owner, repo, prNumber);

        try {
            List<GitHubPullRequestReviewResponse> raw = githubApiWebClient
                    .get()
                    .uri(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_REVIEWS_FETCH_FAILED))
                    .bodyToFlux(GitHubPullRequestReviewResponse.class)
                    .collectList()
                    .block();
            return PullRequestFromApiMapper.toPullRequestReviewFromApiList(raw);
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
    public List<PullRequestCommitFromApi> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching pull request commits for {}/{} - PR #{}", owner, repo, prNumber);

        String endpoint = String.format(GitHubConstants.REPO_PULL_REQUEST_COMMITS_ENDPOINT_TEMPLATE, owner, repo, prNumber);

        try {
            List<GitHubPullRequestCommitResponse> raw = githubApiWebClient
                    .get()
                    .uri(endpoint.replace(GitHubConstants.BASE_API_URL, ""))
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> handleErrorResponse(res, GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED))
                    .bodyToFlux(GitHubPullRequestCommitResponse.class)
                    .collectList()
                    .block();
            return PullRequestFromApiMapper.toPullRequestCommitFromApiList(raw);
        } catch (WebClientResponseException ex) {
            log.error("GitHub API error fetching pull request commits for {}/{} PR #{}: {} - {}", 
                     owner, repo, prNumber, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw handleWebClientException(ex, GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED);
        } catch (Exception ex) {
            log.error("Unexpected error fetching pull request commits for {}/{} PR #{}", owner, repo, prNumber, ex);
            throw new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED + ex.getMessage(), ex);
        }
    }

    private reactor.core.publisher.Mono<? extends Throwable> handleErrorResponse(
            org.springframework.web.reactive.function.client.ClientResponse response, 
            String baseErrorMessage) {
        
        return response.bodyToMono(String.class)
                .map(body -> {
                    HttpStatus status = HttpStatus.valueOf(response.statusCode().value());
                    
                    // Hata detayını logla
                    log.error("GitHub API error: {} - {} - Response: {}", 
                             status.value(), status.getReasonPhrase(), body);
                    
                    // Rate limit hatası
                    if (status == HttpStatus.TOO_MANY_REQUESTS) {
                        return handleRateLimitError(response, body, baseErrorMessage);
                    }
                    
                    // Kimlik doğrulama hatası
                    if (status == HttpStatus.UNAUTHORIZED) {
                        return new GitHubApiException(baseErrorMessage + "Authentication failed. Please check your access token.");
                    }
                    
                    // Erişim yasak hatası
                    if (status == HttpStatus.FORBIDDEN) {
                        return new GitHubApiException(baseErrorMessage + "Access forbidden. You may not have permission to access this resource.");
                    }
                    
                    // Bulunamadı hatası
                    if (status == HttpStatus.NOT_FOUND) {
                        return new GitHubApiException(baseErrorMessage + "Resource not found. The repository or pull request may not exist.");
                    }
                    
                    // Sunucu hatası
                    if (status.is5xxServerError()) {
                        return new GitHubApiException(baseErrorMessage + "GitHub server error. Please try again later.");
                    }
                    
                    // Genel hata
                    return new GitHubApiException(baseErrorMessage + body);
                });
    }

    private GitHubApiException handleWebClientException(WebClientResponseException ex, String baseErrorMessage) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        
        // Rate limit
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return handleRateLimitException(ex, baseErrorMessage);
        }
        
        // Kimlik doğrulama hatası
        if (status == HttpStatus.UNAUTHORIZED) {
            return new GitHubApiException(baseErrorMessage + "Authentication failed. Please check your access token.");
        }
        
        // Erişim yasak
        if (status == HttpStatus.FORBIDDEN) {
            return new GitHubApiException(baseErrorMessage + "Access forbidden. You may not have permission to access this resource.");
        }
        
        // Bulunamadı
        if (status == HttpStatus.NOT_FOUND) {
            return new GitHubApiException(baseErrorMessage + "Resource not found. The repository or pull request may not exist.");
        }
        
        // Sunucu hatası
        if (status.is5xxServerError()) {
            return new GitHubApiException(baseErrorMessage + "GitHub server error. Please try again later.");
        }
        
        // Generic error
        return new GitHubApiException(baseErrorMessage + ex.getResponseBodyAsString(), ex);
    }

    private GitHubRateLimitException handleRateLimitError(
            org.springframework.web.reactive.function.client.ClientResponse response, 
            String body, 
            String baseErrorMessage) {
        
        // Rate limit bilgisini header'lardan al
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

    private GitHubRateLimitException handleRateLimitException(WebClientResponseException ex, String baseErrorMessage) {
        // Rate limit bilgisini header'lardan al
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