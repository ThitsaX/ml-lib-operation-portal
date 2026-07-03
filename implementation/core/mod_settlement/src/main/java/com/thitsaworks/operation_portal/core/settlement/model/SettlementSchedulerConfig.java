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
package com.thitsaworks.operation_portal.core.settlement.model;

import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.SettlementSchedulerConfigId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_settlement_scheduler_config")
@NoArgsConstructor
@Getter
public class SettlementSchedulerConfig {

    @EmbeddedId
    protected SettlementSchedulerConfigId settlementSchedulerConfigId;

    @ManyToOne()
    @MapsId("settlementModelId")
    @JoinColumn(name = "settlement_model_id")
    protected SettlementModel settlementModel;

    public SettlementSchedulerConfig(SettlementModel settlementModel,
                                     SchedulerConfigId schedulerConfigId) {

        this.settlementSchedulerConfigId = new SettlementSchedulerConfigId(settlementModel.getSettlementModelId(),
                                                                           schedulerConfigId.getId());
        this.settlementModel = settlementModel;
    }

}


