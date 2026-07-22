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

package com.thitsaworks.operation_portal.core.revenue_config.command.impl;

import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.revenue_config.command.CreateRevenueConfigCommand;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfig;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigRepository;
import com.thitsaworks.operation_portal.core.revenue_config.validator.RevenueConfigValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateRevenueConfigCommandHandler implements CreateRevenueConfigCommand {

    private final RevenueConfigRepository revenueConfigRepository;

    private final RevenueConfigValidator revenueConfigValidator;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws DomainException {

        if (this.revenueConfigRepository.existsByTaxCodeId(input.taxCodeId())) {
            throw new RevenueConfigException(
                RevenueConfigErrors.TAX_CODE_ALREADY_REGISTERED.format(input.taxCodeId()));
        }

        this.revenueConfigValidator.validate(
            input.category(), input.responsibleMinistryCode(), input.thirdPartyProviderCode(),
            input.golPercentage(), input.ministryPercentage(), input.thirdPartyPercentage(),
            input.sendingDfspPercentage());

        RevenueConfig revenueConfig = new RevenueConfig(
            input.taxCodeId(), input.taxCodeDescription(), input.category(),
            input.responsibleMinistryCode(), input.thirdPartyProviderCode(), input.golPercentage(),
            input.ministryPercentage(), input.thirdPartyPercentage(), input.sendingDfspPercentage(),
            input.createdBy(), input.startDate(), input.status());

        this.revenueConfigRepository.save(revenueConfig);

        return new Output(revenueConfig.getRevenueConfigId());
    }

}
