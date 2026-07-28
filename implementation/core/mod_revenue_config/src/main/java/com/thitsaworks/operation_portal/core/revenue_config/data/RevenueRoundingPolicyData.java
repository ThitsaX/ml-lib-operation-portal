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

import com.thitsaworks.operation_portal.component.common.identifier.RevenueRoundingPolicyId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueRemainderRecipient;
import com.thitsaworks.operation_portal.component.common.type.RevenueRoundingMode;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueRoundingPolicy;

import java.time.Instant;

public record RevenueRoundingPolicyData(RevenueRoundingPolicyId revenueRoundingPolicyId,
                                        RevenueRoundingMode roundingMode,
                                        RevenueRemainderRecipient remainderRecipient,
                                        Instant createdAt,
                                        UserId createdBy) {

    public RevenueRoundingPolicyData(RevenueRoundingPolicy policy) {

        this(
            policy.getRevenueRoundingPolicyId(), policy.getRoundingMode(),
            policy.getRemainderRecipient(), policy.getCreatedAt(), policy.getCreatedBy());
    }
}
