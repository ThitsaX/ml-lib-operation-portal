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

import com.thitsaworks.operation_portal.component.common.identifier.RevenueRoundingPolicyId;
import com.thitsaworks.operation_portal.component.common.type.RevenueRemainderRecipient;
import com.thitsaworks.operation_portal.component.common.type.RevenueRoundingMode;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

import java.time.Instant;

public interface GetRevenueRoundingPolicy
    extends UseCase<GetRevenueRoundingPolicy.Input, GetRevenueRoundingPolicy.Output> {

    record Input() { }

    record Output(RevenueRoundingPolicy revenueRoundingPolicy) { }

    record RevenueRoundingPolicy(RevenueRoundingPolicyId revenueRoundingPolicyId,
                                 RevenueRoundingMode roundingMode,
                                 RevenueRemainderRecipient remainderRecipient,
                                 Instant createdAt,
                                 String createdBy) { }
}
