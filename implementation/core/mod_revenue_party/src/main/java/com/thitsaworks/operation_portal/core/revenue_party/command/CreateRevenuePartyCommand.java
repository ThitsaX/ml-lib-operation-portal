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
package com.thitsaworks.operation_portal.core.revenue_party.command;

import com.thitsaworks.operation_portal.component.common.identifier.RevenuePartyId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenuePartyStatus;
import com.thitsaworks.operation_portal.core.revenue_party.exception.RevenuePartyException;

public interface CreateRevenuePartyCommand {

    record Input(String partyCode,
                 String partyName,
                 String partyType,
                 String description,
                 RevenuePartyStatus status,
                 UserId createdBy) {}

    record Output(boolean created, RevenuePartyId revenuePartyId) {}

    Output execute(Input input) throws RevenuePartyException;
}
