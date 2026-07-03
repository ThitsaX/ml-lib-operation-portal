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
package com.thitsaworks.operation_portal.core.settlement.data;

import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.SettlementModelId;
import com.thitsaworks.operation_portal.core.settlement.model.SettlementModel;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public record SettlementModelData(SettlementModelId settlementModelId,
                                  String name,
                                  String type,
                                  String currencyId,
                                  boolean isActive,
                                  boolean autoCloseWindow,
                                  boolean manualCloseWindow,
                                  String zoneId,
                                  boolean requireLiquidityCheck,
                                  boolean autoPositionReset,
                                  boolean adjustPosition,
                                  Set<SchedulerConfigId> schedulerConfigIds) implements Serializable {

    public SettlementModelData(SettlementModel settlementModel) {

        this(settlementModel.getSettlementModelId(),
             settlementModel.getName(),
             settlementModel.getType(),
             settlementModel.getCurrencyId(),
             settlementModel.isActive(),
             settlementModel.isAutoCloseWindow(),
             settlementModel.isManualCloseWindow(),
             settlementModel.getZoneId(),
             settlementModel.isRequireLiquidityCheck(),
             settlementModel.isAutoPositionReset(),
             settlementModel.isAdjustPosition(),
             settlementModel.getSettlementSchedulerConfigs().stream()
                            .map(schedulerConfig -> new SchedulerConfigId(schedulerConfig.getSettlementSchedulerConfigId()
                                                                                         .getSchedulerConfigId()))
                            .collect(Collectors.toSet()));
    }

}
