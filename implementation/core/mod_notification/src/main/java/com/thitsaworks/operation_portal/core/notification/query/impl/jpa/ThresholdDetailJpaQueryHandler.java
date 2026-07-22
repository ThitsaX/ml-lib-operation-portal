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

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdDetailData;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdDetailRepository;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdDetailQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class ThresholdDetailJpaQueryHandler implements ThresholdDetailQuery {

    private final ThresholdDetailRepository thresholdDetailRepository;

    @Override
    public List<ThresholdDetailData> getAll(ThresholdConfigurationId thresholdConfigurationId,
                                            Boolean status) {

        if (thresholdConfigurationId != null && status != null) {
            return this.thresholdDetailRepository
                       .findAllByThresholdConfigurationIdAndStatusOrderByCurrencyAsc(
                           thresholdConfigurationId, status)
                       .stream()
                       .map(ThresholdDetailData::new)
                       .toList();
        }

        if (thresholdConfigurationId != null) {
            return this.thresholdDetailRepository
                       .findAllByThresholdConfigurationIdOrderByCurrencyAsc(thresholdConfigurationId)
                       .stream()
                       .map(ThresholdDetailData::new)
                       .toList();
        }

        if (status != null) {
            return this.thresholdDetailRepository
                       .findAllByStatusOrderByThresholdConfigurationIdAscCurrencyAsc(status)
                       .stream()
                       .map(ThresholdDetailData::new)
                       .toList();
        }

        return this.thresholdDetailRepository.findAllByOrderByThresholdConfigurationIdAscCurrencyAsc()
                                             .stream()
                                             .map(ThresholdDetailData::new)
                                             .toList();
    }

    @Override
    public Optional<ThresholdDetailData> get(ThresholdDetailId thresholdDetailId) {

        return this.thresholdDetailRepository.findById(thresholdDetailId)
                                             .map(ThresholdDetailData::new);
    }
}
