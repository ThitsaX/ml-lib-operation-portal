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
package com.thitsaworks.operation_portal.core.notification.command.impl;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.core.notification.command.CreateThresholdDetailCommand;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdConfiguration;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdConfigurationRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateThresholdDetailCommandHandler implements CreateThresholdDetailCommand {

    private final ThresholdConfigurationRepository thresholdConfigurationRepository;

    private final ThresholdDetailRepository thresholdDetailRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        ThresholdConfiguration configuration = this.thresholdConfigurationRepository
            .findById(input.thresholdConfigurationId())
            .orElseThrow(() -> new InputException(
                new ErrorMessage(
                    "THRESHOLD_CONFIGURATION_NOT_FOUND",
                    "Threshold configuration was not found.")));

        if (configuration.getScopeType() != ThresholdScopeType.DFSP) {
            throw new InputException(
                new ErrorMessage(
                    "THRESHOLD_DETAIL_REQUIRES_DFSP_CONFIGURATION",
                    "Threshold detail must reference a DFSP configuration."));
        }

        String currency = ThresholdDetail.normalizeCurrency(input.currency());

        if (this.thresholdDetailRepository
                .existsByThresholdConfigurationIdAndCurrencyAndStatusTrue(
                    input.thresholdConfigurationId(), currency)) {

            throw new InputException(
                new ErrorMessage(
                    "THRESHOLD_DETAIL_ACTIVE_CURRENCY_ALREADY_EXISTS",
                    "An active threshold detail already exists for this configuration and currency."));
        }

        ThresholdDetail detail = new ThresholdDetail(
            input.thresholdConfigurationId(),
            currency,
            input.visualConfig(),
            input.ndcConfig(),
            input.createdBy());

        this.thresholdDetailRepository.save(detail);

        return new Output(detail.getThresholdDetailId());
    }
}
