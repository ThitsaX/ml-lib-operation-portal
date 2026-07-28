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
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;
import com.thitsaworks.operation_portal.component.misc.util.BigDecimalUtil;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface GetRevenueConfigList
    extends UseCase<GetRevenueConfigList.Input, GetRevenueConfigList.Output> {

    String DEFAULT_SORT_FIELD = "createdAt";

    record Input(String sortBy, Sort.Direction sortDirection) {

        public Input {

            sortBy = sortBy != null ? sortBy : DEFAULT_SORT_FIELD;
            sortDirection = sortDirection != null ? sortDirection : Sort.Direction.DESC;
        }

    }

    record Output(@NotNull List<@NotNull RevenueConfig> revenueConfigs) {

        public Output {

            if (revenueConfigs == null) {
                throw new IllegalArgumentException("Revenue configuration list cannot be null");
            }
        }

    }

    record RevenueConfig(RevenueConfigId id,
                         String taxCodeId,
                         String taxCodeDescription,
                         String category,
                         String responsibleMinistryCode,
                         String responsibleMinistryName,
                         String thirdPartyProviderCode,
                         String thirdPartyProviderName,
                         BigDecimal golPercentage,
                         BigDecimal ministryPercentage,
                         BigDecimal thirdPartyPercentage,
                         BigDecimal sendingDfspPercentage,
                         String status,
                         Instant effectiveDate,
                         Instant respondedDate,
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
