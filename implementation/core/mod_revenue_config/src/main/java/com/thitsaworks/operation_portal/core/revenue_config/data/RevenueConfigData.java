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

package com.thitsaworks.operation_portal.core.revenue_config.data;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfig;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueConfigData(RevenueConfigId revenueConfigId,
                                String taxCodeId,
                                String taxCodeDescription,
                                RevenueConfigCategory category,
                                Long responsibleMinistryId,
                                Long thirdPartyProviderId,
                                BigDecimal golPercentage,
                                BigDecimal ministryPercentage,
                                BigDecimal thirdPartyPercentage,
                                BigDecimal sendingDfspPercentage,
                                RevenueConfigStatus status,
                                Instant createdAt,
                                Instant updatedAt,
                                UserId createdBy,
                                UserId updatedBy) {

    public RevenueConfigData(RevenueConfig revenueConfig) {

        this(
            revenueConfig.getRevenueConfigId(), revenueConfig.getTaxCodeId(),
            revenueConfig.getTaxCodeDescription(), revenueConfig.getCategory(),
            revenueConfig.getResponsibleMinistryId(), revenueConfig.getThirdPartyProviderId(),
            revenueConfig.getGolPercentage(), revenueConfig.getMinistryPercentage(),
            revenueConfig.getThirdPartyPercentage(), revenueConfig.getSendingDfspPercentage(),
            revenueConfig.getStatus(), revenueConfig.getCreatedAt(), revenueConfig.getUpdatedAt(),
            revenueConfig.getCreatedBy(), revenueConfig.getUpdatedBy());
    }

}
