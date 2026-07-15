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

import com.thitsaworks.operation_portal.core.notification.data.ThresholdGateDecision;
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
    public Optional<ThresholdConfigurationData> getSchemeConfiguration() {

        return this.thresholdConfigurationRepository
                   .findFirstByScopeTypeAndStatus(
                       ThresholdScopeType.SCHEME, NdcConfigurationStatus.ACTIVE)
                   .map(ThresholdConfigurationData::new);
    }

    @Override
    public Optional<ThresholdConfigurationData> getDfspConfiguration(String dfspId) {

        return this.thresholdConfigurationRepository
                   .findFirstByScopeTypeAndDfspIdAndStatus(
                       ThresholdScopeType.DFSP, dfspId, NdcConfigurationStatus.ACTIVE)
                   .map(ThresholdConfigurationData::new);
    }

    @Override
    public ThresholdGateDecision checkGate(String dfspId) {

        if (dfspId == null || dfspId.isBlank()) {
            return new ThresholdGateDecision(
                false,
                false,
                false,
                "DFSP_ID_MISSING"
            );
        }

        var schemeConfiguration = getSchemeConfiguration();

        if (schemeConfiguration.isEmpty()) {
            return new ThresholdGateDecision(
                false,
                false,
                false,
                "SCHEME_CONFIGURATION_MISSING_OR_INACTIVE"
            );
        }

        if (!schemeConfiguration.get().thresholdEnabled()) {
            return new ThresholdGateDecision(
                false,
                false,
                false,
                "SCHEME_GATE_OFF"
            );
        }

        var dfspConfiguration = getDfspConfiguration(dfspId);

        if (dfspConfiguration.isEmpty()) {
            return new ThresholdGateDecision(
                false,
                true,
                false,
                "DFSP_CONFIGURATION_MISSING_OR_INACTIVE"
            );
        }

        if (!dfspConfiguration.get().thresholdEnabled()) {
            return new ThresholdGateDecision(
                false,
                true,
                false,
                "DFSP_GATE_OFF"
            );
        }

        return new ThresholdGateDecision(
            true,
            true,
            true,
            "SCHEME_AND_DFSP_GATES_ON"
        );
    }

}
