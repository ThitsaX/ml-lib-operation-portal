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
package com.thitsaworks.operation_portal.core.revenue_party.data;

import com.thitsaworks.operation_portal.core.revenue_party.model.RevenueParty;

import java.io.Serializable;

public record RevenuePartyData(String revenuePartyId,
                               String partyCode,
                               String partyName,
                               String partyType,
                               String description,
                               boolean isActive,
                               String createdBy,
                               String updatedBy,
                               Long createdDate,
                               Long updatedDate) implements Serializable {

    public RevenuePartyData(RevenueParty revenueParty) {

        this(revenueParty.getRevenuePartyId().getId().toString(),
             revenueParty.getPartyCode(),
             revenueParty.getPartyName(),
             revenueParty.getPartyType(),
             revenueParty.getDescription(),
             revenueParty.isActive(),
             revenueParty.getCreatedBy().getId().toString(),
             revenueParty.getUpdatedBy() == null ? null : revenueParty.getUpdatedBy().getId().toString(),
             revenueParty.getCreatedAt().getEpochSecond(),
             revenueParty.getUpdatedAt().getEpochSecond());
    }

}
