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
package com.thitsaworks.operation_portal.core.settlement.command.impl;

import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.settlement.command.CreateSettlementModelCommand;
import com.thitsaworks.operation_portal.core.settlement.exception.SettlementErrors;
import com.thitsaworks.operation_portal.core.settlement.exception.SettlementException;
import com.thitsaworks.operation_portal.core.settlement.model.SettlementModel;
import com.thitsaworks.operation_portal.core.settlement.repository.SettlementModelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateSettlementModelCommandHandler implements CreateSettlementModelCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CreateSettlementModelCommandHandler.class);

    private final SettlementModelRepository settlementModelRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws SettlementException {

        Optional<SettlementModel> optionalSettlementModel = this.settlementModelRepository.findOne(
                SettlementModelRepository.Filters.findByName(input.name()));

        if (optionalSettlementModel.isPresent()) {
            throw new SettlementException(SettlementErrors.SETTLEMENT_MODEL_ALREADY_REGISTERED.format(input.name()));
        }

        SettlementModel settlementModel = new SettlementModel(input.name(),
                                                              input.type(),
                                                              input.currencyId(),
                                                              input.isActive(),
                                                              input.autoCloseWindow(),
                                                              input.manualCloseWindow(),
                                                              input.zoneId(),
                                                              input.requireLiquidityCheck(),
                                                              input.autoPositionReset(),
                                                              input.adjustPosition());

        if (input.autoCloseWindow() && !input.schedulerConfigIdList().isEmpty()) {

            for (SchedulerConfigId schedulerConfigId : input.schedulerConfigIdList()) {
                settlementModel.addSchedulerConfig(schedulerConfigId);
            }

        }

        this.settlementModelRepository.save(settlementModel);

        return new Output(true, settlementModel.getSettlementModelId());

    }

}
