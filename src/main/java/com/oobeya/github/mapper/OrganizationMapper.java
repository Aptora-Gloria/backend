package com.oobeya.github.mapper;

import com.oobeya.github.dto.response.OrganizationDto;
import com.oobeya.github.entity.OrganizationEntity;

public class OrganizationMapper {

    public static OrganizationEntity toEntity(OrganizationDto dto) {
        return OrganizationEntity.builder()
                .login(dto.login())
                .description(dto.description())
                .avatarUrl(dto.avatarUrl())
                .build();
    }
}

