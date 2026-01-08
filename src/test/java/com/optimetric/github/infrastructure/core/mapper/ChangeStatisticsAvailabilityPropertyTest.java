package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.ChangeStatsDto;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.infrastructure.persistence.entity.*;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.shared.domain.OwnerType;
import net.jqwik.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for change statistics availability indicator.
 * **Feature: pull-request-ingestion, Property 8: Change Statistics Availability Indicator**
 * **Validates: Requirements 7.1, 7.2, 7.5**
 */
public class ChangeStatisticsAvailabilityPropertyTest {

    @Property(tries = 100)
    void changeStatsAvailableShouldBeTrueWhenAllStatisticsPresent(
            @ForAll("validPullRequestEntityWithCompleteChangeStats") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = PullRequestMapper.toDto(pullRequest);
        
        // Then - changeStats.available should be true (Requirement 7.1)
        assertThat(dto.changeStats().available()).isTrue();
        assertThat(dto.changeStats().additions()).isEqualTo(pullRequest.getAdditions());
        assertThat(dto.changeStats().deletions()).isEqualTo(pullRequest.getDeletions());
        assertThat(dto.changeStats().changedFiles()).isEqualTo(pullRequest.getChangedFiles());
        assertThat(dto.changeStats().syncedAt()).isEqualTo(pullRequest.getChangeStatsSyncedAt());
        
        // Verify all change statistics are non-null
        assertThat(pullRequest.getAdditions()).isNotNull();
        assertThat(pullRequest.getDeletions()).isNotNull();
        assertThat(pullRequest.getChangedFiles()).isNotNull();
    }

    @Property(tries = 100)
    void changeStatsAvailableShouldBeFalseWhenAnyStatisticMissing(
            @ForAll("validPullRequestEntityWithIncompleteChangeStats") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = PullRequestMapper.toDto(pullRequest);
        
        // Then - changeStats.available should be false (Requirement 7.2)
        assertThat(dto.changeStats().available()).isFalse();
        
        // Verify at least one change statistic is null
        boolean hasNullStats = pullRequest.getAdditions() == null || 
                              pullRequest.getDeletions() == null || 
                              pullRequest.getChangedFiles() == null;
        assertThat(hasNullStats).isTrue();
    }

    @Property(tries = 100)
    void changeStatsAvailableShouldBeFalseWhenAllStatisticsNull(
            @ForAll("validPullRequestEntityWithNullChangeStats") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = PullRequestMapper.toDto(pullRequest);
        
        // Then - changeStats.available should be false (Requirement 7.2)
        assertThat(dto.changeStats().available()).isFalse();
        assertThat(dto.changeStats().additions()).isNull();
        assertThat(dto.changeStats().deletions()).isNull();
        assertThat(dto.changeStats().changedFiles()).isNull();
        assertThat(dto.changeStats().syncedAt()).isNull();
        
        // Verify all change statistics are null
        assertThat(pullRequest.getAdditions()).isNull();
        assertThat(pullRequest.getDeletions()).isNull();
        assertThat(pullRequest.getChangedFiles()).isNull();
        assertThat(pullRequest.getChangeStatsSyncedAt()).isNull();
    }

    @Property(tries = 100)
    void lastSyncedAtTimestampShouldBeIncludedInDto(
            @ForAll("validPullRequestEntityWithTimestamp") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = PullRequestMapper.toDto(pullRequest);
        
        // Then - lastSyncedAt should be included (Requirement 7.5)
        assertThat(dto.lastSyncedAt()).isEqualTo(pullRequest.getLastSyncedAt());
        assertThat(dto.lastSyncedAt()).isNotNull();
    }

    @Property(tries = 100)
    void changeStatsAvailabilityLogicShouldBeConsistent(
            @ForAll("validPullRequestEntityWithRandomChangeStats") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = PullRequestMapper.toDto(pullRequest);
        
        // Then - availability should match the presence of all statistics
        boolean expectedAvailable = pullRequest.getAdditions() != null && 
                                   pullRequest.getDeletions() != null && 
                                   pullRequest.getChangedFiles() != null;
        
        assertThat(dto.changeStats().available()).isEqualTo(expectedAvailable);
        
        // Verify consistency with ChangeStatsDto.of() method
        ChangeStatsDto expectedDto = ChangeStatsDto.of(
                pullRequest.getAdditions(),
                pullRequest.getDeletions(),
                pullRequest.getChangedFiles(),
                pullRequest.getChangeStatsSyncedAt()
        );
        
        assertThat(dto.changeStats().available()).isEqualTo(expectedDto.available());
        assertThat(dto.changeStats().additions()).isEqualTo(expectedDto.additions());
        assertThat(dto.changeStats().deletions()).isEqualTo(expectedDto.deletions());
        assertThat(dto.changeStats().changedFiles()).isEqualTo(expectedDto.changedFiles());
        assertThat(dto.changeStats().syncedAt()).isEqualTo(expectedDto.syncedAt());
    }

    @Property(tries = 100)
    void changeStatsSyncedAtTimestampShouldReflectDataFreshness(
            @ForAll("validPullRequestEntityWithChangeStatsSyncedAt") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = PullRequestMapper.toDto(pullRequest);
        
        // Then - syncedAt should always match the entity value (Requirement 7.5)
        assertThat(dto.changeStats().syncedAt()).isEqualTo(pullRequest.getChangeStatsSyncedAt());
        
        // The availability logic should be independent of syncedAt timestamp
        boolean expectedAvailable = pullRequest.getAdditions() != null && 
                                   pullRequest.getDeletions() != null && 
                                   pullRequest.getChangedFiles() != null;
        assertThat(dto.changeStats().available()).isEqualTo(expectedAvailable);
    }

    // Data generators

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithCompleteChangeStats() {
        return Combinators.combine(
                validBasePullRequestEntity(),
                Arbitraries.integers().between(0, 10000), // additions
                Arbitraries.integers().between(0, 10000), // deletions
                Arbitraries.integers().between(1, 1000),  // changedFiles
                validOffsetDateTime()                     // changeStatsSyncedAt
        ).as((base, additions, deletions, changedFiles, syncedAt) -> {
            base.setAdditions(additions);
            base.setDeletions(deletions);
            base.setChangedFiles(changedFiles);
            base.setChangeStatsSyncedAt(syncedAt);
            return base;
        });
    }

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithIncompleteChangeStats() {
        return Combinators.combine(
                validBasePullRequestEntity(),
                Arbitraries.integers().between(0, 10000).optional(), // additions (may be null)
                Arbitraries.integers().between(0, 10000).optional(), // deletions (may be null)
                Arbitraries.integers().between(1, 1000).optional(),  // changedFiles (may be null)
                validOffsetDateTime().optional()                     // changeStatsSyncedAt (may be null)
        ).as((base, additions, deletions, changedFiles, syncedAt) -> {
            base.setAdditions(additions.orElse(null));
            base.setDeletions(deletions.orElse(null));
            base.setChangedFiles(changedFiles.orElse(null));
            base.setChangeStatsSyncedAt(syncedAt.orElse(null));
            return base;
        }).filter(entity -> {
            // Ensure at least one statistic is null (incomplete)
            return entity.getAdditions() == null || 
                   entity.getDeletions() == null || 
                   entity.getChangedFiles() == null;
        });
    }

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithNullChangeStats() {
        return validBasePullRequestEntity().map(base -> {
            base.setAdditions(null);
            base.setDeletions(null);
            base.setChangedFiles(null);
            base.setChangeStatsSyncedAt(null);
            return base;
        });
    }

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithTimestamp() {
        return Combinators.combine(
                validBasePullRequestEntity(),
                validOffsetDateTime()
        ).as((base, timestamp) -> {
            base.setLastSyncedAt(timestamp);
            return base;
        });
    }

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithRandomChangeStats() {
        return Combinators.combine(
                validBasePullRequestEntity(),
                Arbitraries.integers().between(0, 10000).optional(), // additions
                Arbitraries.integers().between(0, 10000).optional(), // deletions
                Arbitraries.integers().between(1, 1000).optional(),  // changedFiles
                validOffsetDateTime().optional()                     // changeStatsSyncedAt
        ).as((base, additions, deletions, changedFiles, syncedAt) -> {
            base.setAdditions(additions.orElse(null));
            base.setDeletions(deletions.orElse(null));
            base.setChangedFiles(changedFiles.orElse(null));
            base.setChangeStatsSyncedAt(syncedAt.orElse(null));
            return base;
        });
    }

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithChangeStatsSyncedAt() {
        return Combinators.combine(
                validBasePullRequestEntity(),
                Arbitraries.integers().between(0, 10000).optional(), // additions
                Arbitraries.integers().between(0, 10000).optional(), // deletions
                Arbitraries.integers().between(1, 1000).optional(),  // changedFiles
                validOffsetDateTime().optional()                     // changeStatsSyncedAt
        ).as((base, additions, deletions, changedFiles, syncedAt) -> {
            base.setAdditions(additions.orElse(null));
            base.setDeletions(deletions.orElse(null));
            base.setChangedFiles(changedFiles.orElse(null));
            base.setChangeStatsSyncedAt(syncedAt.orElse(null));
            return base;
        });
    }

    // Helper generators

    @Provide
    Arbitrary<PullRequestEntity> validBasePullRequestEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.of(DataLevel.class),         // dataLevel
                validOffsetDateTime()                    // updatedAt
        ).as((githubPrId, prNumber, repository, dataLevel, updatedAt) ->
                PullRequestEntity.builder()
                        .id(githubPrId) // Use githubPrId as entity ID for simplicity
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(dataLevel)
                        .state("open")
                        .draft(false)
                        .title("Test PR " + githubPrId)
                        .authorLogin("testuser" + githubPrId)
                        .authorGithubId(12345L + githubPrId)
                        .htmlUrl("https://github.com/test/repo/pull/" + prNumber)
                        .createdAt(updatedAt.minusHours(1))
                        .updatedAt(updatedAt)
                        .lastSyncedAt(updatedAt.plusMinutes(5))
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    @Provide
    Arbitrary<RepositoryEntity> validRepositoryEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubRepoId
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(255), // name
                validRepoFullName(),                     // fullName
                validUser(),                             // user
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // ownerLogin
                Arbitraries.of(OwnerType.class)          // ownerType
        ).as((githubRepoId, name, fullName, user, ownerLogin, ownerType) ->
                RepositoryEntity.builder()
                        .id(githubRepoId) // Use githubRepoId as entity ID for simplicity
                        .githubRepoId(githubRepoId)
                        .name(name)
                        .fullName(fullName)
                        .user(user)
                        .ownerLogin(ownerLogin)
                        .ownerType(ownerType)
                        .build()
        );
    }

    @Provide
    Arbitrary<User> validUser() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubUserId
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // login
                Arbitraries.of(OwnerType.class)          // ownerType
        ).as((githubUserId, login, ownerType) ->
                User.builder()
                        .id(githubUserId) // Use githubUserId as entity ID for simplicity
                        .githubUserId(githubUserId)
                        .login(login)
                        .ownerType(ownerType)
                        .build()
        );
    }

    @Provide
    Arbitrary<OffsetDateTime> validOffsetDateTime() {
        return Arbitraries.longs()
                .between(1640995200L, 1735689600L) // 2022-01-01 to 2025-01-01
                .map(epochSecond -> OffsetDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(epochSecond),
                        ZoneOffset.UTC
                ));
    }

    @Provide
    Arbitrary<String> validRepoFullName() {
        return Combinators.combine(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50),
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50)
        ).as((owner, repo) -> owner + "/" + repo);
    }
}