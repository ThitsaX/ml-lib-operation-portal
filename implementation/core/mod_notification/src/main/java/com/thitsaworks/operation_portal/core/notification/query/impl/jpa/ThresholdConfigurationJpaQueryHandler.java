/*
 * Copyright (c) 2024-2026 ThitsaWorks Pte. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thitsaworks.operation_portal.core.notification.query.impl.jpa;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdConfigurationData;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdConfigurationRepository;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class ThresholdConfigurationJpaQueryHandler implements ThresholdConfigurationQuery {

    private final ThresholdConfigurationRepository thresholdConfigurationRepository;

    @Override
    public List<ThresholdConfigurationData> getAll() {

        return this.thresholdConfigurationRepository.findAll()
                                                    .stream()
                                                    .sorted(Comparator.comparing(config -> config.getId().getId()))
                                                    .map(ThresholdConfigurationData::new)
                                                    .toList();
    }

    @Override
    public List<ThresholdConfigurationData> getByScheme(String schemeId) {

        return this.thresholdConfigurationRepository.findBySchemeId(schemeId)
                                                    .stream()
                                                    .map(ThresholdConfigurationData::new)
                                                    .toList();
    }

    @Override
    public Optional<ThresholdConfigurationData> getSchemeConfiguration(String schemeId) {

        return this.thresholdConfigurationRepository
                   .findFirstByScopeTypeAndSchemeIdAndStatus(
                       ThresholdScopeType.SCHEME, schemeId, NdcConfigurationStatus.ACTIVE)
                   .map(ThresholdConfigurationData::new);
    }

    @Override
    public Optional<ThresholdConfigurationData> getDfspConfiguration(String schemeId, String dfspId) {

        return this.thresholdConfigurationRepository
                   .findFirstByScopeTypeAndSchemeIdAndDfspIdAndStatus(
                       ThresholdScopeType.DFSP, schemeId, dfspId, NdcConfigurationStatus.ACTIVE)
                   .map(ThresholdConfigurationData::new);
    }

}
