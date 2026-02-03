package com.optimetric.github.api.mapper;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.application.dto.CommitInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Application DTO to API DTO for Commit endpoints.
 */
public final class CommitApiMapper {

    private CommitApiMapper() {}

    public static CommitDto toCommitDto(CommitInfo info) {
        if (info == null) return null;
        return new CommitDto(
                info.sha(),
                info.nodeId(),
                new CommitDto.CommitInfo(
                        info.message(),
                        null, // CommitAuthor - not in application DTO
                        null  // CommitCommitter
                ),
                new CommitDto.AuthorInfo(info.authorLogin()),
                new CommitDto.CommitterInfo(info.committerLogin()),
                info.parentShas() == null ? null : info.parentShas().stream()
                        .map(CommitDto.ParentInfo::new)
                        .collect(Collectors.toList()),
                info.htmlUrl(),
                info.apiUrl()
        );
    }

    public static List<CommitDto> toCommitDtos(List<CommitInfo> list) {
        return list == null ? List.of() : list.stream().map(CommitApiMapper::toCommitDto).collect(Collectors.toList());
    }
}
