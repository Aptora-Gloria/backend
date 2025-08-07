package com.oobeya.github.service.impl;

import com.oobeya.github.dto.response.OrganizationDto;
import com.oobeya.github.entity.OrganizationEntity;
import com.oobeya.github.mapper.OrganizationMapper;
import com.oobeya.github.repository.OrganizationRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final ApiClientI apiClient;
    private final OrganizationRepository organizationRepository;

    @Override
    public List<OrganizationEntity> initOrganizations(String accessToken) {
        List<OrganizationDto> orgDtos = apiClient.getUserOrganizations(accessToken);

        return orgDtos.stream()
                .map(dto -> organizationRepository.findByLogin(dto.login())
                        .orElseGet(() -> {
                            OrganizationEntity entity = OrganizationMapper.toEntity(dto);
                            return organizationRepository.save(entity);
                        }))
                .toList();
    }
}
