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
package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.misc.util.BigDecimalUtil;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

import java.math.BigDecimal;

public interface GetRevenueConfigById
    extends UseCase<GetRevenueConfigById.Input, GetRevenueConfigById.Output> {

    record Input(RevenueConfigId revenueConfigId) { }

    record Output(RevenueConfig revenueConfig) { }

    record RevenueConfig(Long id,
                         String taxCodeId,
                         String taxCodeDescription,
                         String category,
                         Long responsibleMinistryId,
                         String responsibleMinistryName,
                         Long thirdPartyProviderId,
                         String thirdPartyProviderName,
                         BigDecimal golPercentage,
                         BigDecimal ministryPercentage,
                         BigDecimal thirdPartyPercentage,
                         BigDecimal sendingDfspPercentage,
                         boolean status,
                         Long createdAt,
                         String createdBy,
                         Long updatedAt,
                         String updatedBy) {

        public RevenueConfig {
            golPercentage = BigDecimalUtil.trimTrailingZeros(golPercentage);
            ministryPercentage = BigDecimalUtil.trimTrailingZeros(ministryPercentage);
            thirdPartyPercentage = BigDecimalUtil.trimTrailingZeros(thirdPartyPercentage);
            sendingDfspPercentage = BigDecimalUtil.trimTrailingZeros(sendingDfspPercentage);
        }
    }
}
