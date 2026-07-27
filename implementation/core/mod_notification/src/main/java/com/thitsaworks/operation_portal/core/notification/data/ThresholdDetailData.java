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
package com.thitsaworks.operation_portal.core.notification.data;

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;

import java.math.BigDecimal;
import java.time.Instant;

public record ThresholdDetailData(
    ThresholdDetailId thresholdDetailId,
    ThresholdConfigurationId thresholdConfigurationId,
    String currency,
    BigDecimal visualConfig,
    BigDecimal ndcConfig,
    boolean status,
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy
) {

    public ThresholdDetailData(ThresholdDetail detail) {

        this(
            detail.getThresholdDetailId(),
            detail.getThresholdConfigurationId(),
            detail.getCurrency(),
            detail.getVisualConfig(),
            detail.getNdcConfig(),
            detail.isStatus(),
            detail.getCreatedAt(),
            detail.getCreatedBy(),
            detail.getUpdatedAt(),
            detail.getUpdatedBy()
        );
    }
}
