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

import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.notification.data.DfspThresholdDetailData;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdDetailRepository;
import com.thitsaworks.operation_portal.core.notification.query.DfspThresholdDetailQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class DfspThresholdDetailJpaQueryHandler implements DfspThresholdDetailQuery {

    private final ThresholdDetailRepository thresholdDetailRepository;

    @Override
    public List<DfspThresholdDetailData> getDfspThresholdDetails(String dfspId) {
        
        List<ThresholdDetail> details = this.thresholdDetailRepository.findDfspThresholdDetails(
            ThresholdScopeType.DFSP,
            dfspId,
            true,
            NdcConfigurationStatus.ACTIVE,
            true
        );

        return details.stream()
            .map(detail -> new DfspThresholdDetailData(
                dfspId,
                detail.getCurrency(),
                detail.getVisualConfig()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<DfspThresholdDetailData> getAllDfspThresholdDetails() {
        
        List<Object[]> results = this.thresholdDetailRepository.findAllDfspThresholdDetails(
            ThresholdScopeType.DFSP,
            true,
            NdcConfigurationStatus.ACTIVE,
            true
        );

        return results.stream()
            .map(result -> {
                ThresholdDetail detail = (ThresholdDetail) result[0];
                String dfspId = (String) result[1];
                return new DfspThresholdDetailData(
                    dfspId,
                    detail.getCurrency(),
                    detail.getVisualConfig()
                );
            })
            .collect(Collectors.toList());
    }
}
