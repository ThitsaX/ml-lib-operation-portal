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

import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.notification.command.CreateThresholdConfigurationCommand;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdConfigurationData;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdConfiguration;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateThresholdConfigurationCommandHandler implements CreateThresholdConfigurationCommand {

    private final ThresholdConfigurationRepository thresholdConfigurationRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        if (input.scopeType() == ThresholdScopeType.SCHEME
            && this.thresholdConfigurationRepository
                   .existsByScopeTypeAndDfspIdIsNull(ThresholdScopeType.SCHEME)) {

            throw new InputException(
                new ErrorMessage(
                    "SCHEME_CONFIGURATION_ALREADY_EXISTS",
                    "Only one scheme configuration is allowed."));
        }

        ThresholdConfiguration configuration = new ThresholdConfiguration(
            input.scopeType(),
            input.dfspId(),
            input.thresholdEnabled(),
            input.createdBy());

        this.thresholdConfigurationRepository.save(configuration);

        return new Output(configuration.getThresholdConfigurationId());
    }

}
