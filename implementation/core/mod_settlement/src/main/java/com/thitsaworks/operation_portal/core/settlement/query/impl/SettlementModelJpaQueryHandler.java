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
package com.thitsaworks.operation_portal.core.settlement.query.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.SettlementModelId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.settlement.data.SettlementModelData;
import com.thitsaworks.operation_portal.core.settlement.exception.SettlementErrors;
import com.thitsaworks.operation_portal.core.settlement.exception.SettlementException;
import com.thitsaworks.operation_portal.core.settlement.model.QSettlementModel;
import com.thitsaworks.operation_portal.core.settlement.model.SettlementModel;
import com.thitsaworks.operation_portal.core.settlement.query.SettlementModelQuery;
import com.thitsaworks.operation_portal.core.settlement.repository.SettlementModelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class SettlementModelJpaQueryHandler implements SettlementModelQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettlementModelJpaQueryHandler.class);

    private final SettlementModelRepository settlementModelRepository;

    private final QSettlementModel settlementModel = QSettlementModel.settlementModel;

    @Override
    public List<SettlementModelData> getSettlementModels() {

        List<SettlementModel> settlementModels = (List<SettlementModel>) this.settlementModelRepository.findAll();

        return settlementModels.stream()
                               .map(SettlementModelData::new)
                               .toList();
    }

    @Override
    public SettlementModelData get(SettlementModelId settlementModelId) throws SettlementException {

        BooleanExpression predicate = this.settlementModel.settlementModelId.eq(settlementModelId);

        Optional<SettlementModel> optionalSettlementModel = this.settlementModelRepository.findOne(predicate);

        if (optionalSettlementModel.isEmpty()) {

            throw new SettlementException(SettlementErrors.SETTLEMENT_MODEL_NOT_FOUND.format(settlementModelId.getId()
                                                                                                              .toString()));
        }

        return new SettlementModelData(optionalSettlementModel.get());
    }

    @Override
    public Optional<SettlementModelData> get(String settlementModelName) {

        Optional<SettlementModel> optionalSettlementModel = this.settlementModelRepository.findOne(
                SettlementModelRepository.Filters.findByName(settlementModelName));

        return optionalSettlementModel.map(SettlementModelData::new);

    }

    @Override
    public SettlementModelData get(SchedulerConfigId schedulerConfigId) throws SettlementException {

        Optional<SettlementModel> optionalSettlementModel = this.settlementModelRepository.findOne(
                SettlementModelRepository.Filters.findBySchedulerConfigId(schedulerConfigId));

        if (optionalSettlementModel.isEmpty()) {

            throw new SettlementException(SettlementErrors.SETTLEMENT_MODEL_SCHEDULER_NOT_FOUND.format(schedulerConfigId.getId()
                                                                                                                        .toString()));
        }

        return new SettlementModelData(optionalSettlementModel.get());
    }

}
