package com.optimetric.github.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.api.response.BranchDto;
import com.optimetric.github.application.dto.BranchInfo;

public final class BranchApiMapper {

    private BranchApiMapper() {}

    public static BranchDto toBranchDto(BranchInfo info) {
        return info == null ? null : new BranchDto(info.name());
    }

    public static List<BranchDto> toBranchDtos(List<BranchInfo> list) {
        return list == null ? List.of() : list.stream().map(BranchApiMapper::toBranchDto).collect(Collectors.toList());
    }
}
