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
import com.thitsaworks.operation_portal.core.notification.command.CreateThresholdDetailCommand;
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

        if (!this.thresholdConfigurationRepository.existsById(input.thresholdConfigurationId())) {
            throw new InputException(
                new ErrorMessage(
                    "THRESHOLD_CONFIGURATION_NOT_FOUND",
                    "Threshold configuration was not found."));
        }

        if (this.thresholdDetailRepository.existsByThresholdConfigurationIdAndCurrency(
            input.thresholdConfigurationId(), input.currency())) {

            throw new InputException(
                new ErrorMessage(
                    "THRESHOLD_DETAIL_CURRENCY_ALREADY_EXISTS",
                    "Threshold detail already exists for this configuration and currency."));
        }

        if (this.thresholdDetailRepository.existsByParticipantCurrencyId(input.participantCurrencyId())) {
            throw new InputException(
                new ErrorMessage(
                    "THRESHOLD_DETAIL_PARTICIPANT_CURRENCY_ALREADY_EXISTS",
                    "Threshold detail already exists for this participant currency."));
        }

        ThresholdDetail detail = new ThresholdDetail(
            input.thresholdConfigurationId(),
            input.participantCurrencyId(),
            input.dfspId(),
            input.currency(),
            input.visualConfig(),
            input.ndcConfig(),
            input.createdBy());

        this.thresholdDetailRepository.save(detail);

        return new Output(detail.getThresholdDetailId());
    }
}
