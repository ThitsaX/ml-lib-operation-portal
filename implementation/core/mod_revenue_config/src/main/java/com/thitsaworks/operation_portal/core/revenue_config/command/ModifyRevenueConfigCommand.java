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

package com.thitsaworks.operation_portal.core.revenue_config.command;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;

public interface ModifyRevenueConfigCommand {

    record Input(RevenueConfigId revenueConfigId,
                 String taxCodeId,
                 String taxCodeDescription,
                 RevenueConfigCategory category,
                 String responsibleMinistryCode,
                 String thirdPartyProviderCode,
                 BigDecimal golPercentage,
                 BigDecimal ministryPercentage,
                 BigDecimal thirdPartyPercentage,
                 BigDecimal sendingDfspPercentage,
                 UserId updatedBy,
                 Instant startDate) { }

    record Output(RevenueConfigId revenueConfigId, boolean modified) { }

    Output execute(Input input) throws DomainException;

}
