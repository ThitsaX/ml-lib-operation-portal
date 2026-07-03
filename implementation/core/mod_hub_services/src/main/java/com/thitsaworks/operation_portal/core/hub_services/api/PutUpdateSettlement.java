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
package com.thitsaworks.operation_portal.core.hub_services.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementParticipant;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementWindow;

import java.util.List;

public class PutUpdateSettlement {

    public record Request(List<SettlementParticipant> participants) {}

    public record Response(
            @JsonProperty("id") String id,
            @JsonProperty("state") String state,
            @JsonProperty("createdDate") String createdDate,
            @JsonProperty("settlementWindows") List<SettlementWindow> settlementWindows,
            @JsonProperty("participants") List<SettlementParticipant> participants) {}

}

