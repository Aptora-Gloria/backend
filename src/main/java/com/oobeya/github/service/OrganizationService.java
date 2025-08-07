package com.oobeya.github.service;

import com.oobeya.github.entity.OrganizationEntity;
import java.util.List;

public interface OrganizationService {
    List<OrganizationEntity> initOrganizations(String accessToken);
}
