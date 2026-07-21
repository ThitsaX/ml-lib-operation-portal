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
import com.thitsaworks.operation_portal.core.revenue_config.command.ModifyRevenueConfigCommand;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfig;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfigHistory;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigHistoryRepository;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigRepository;
import com.thitsaworks.operation_portal.core.revenue_config.validator.RevenueConfigValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModifyRevenueConfigCommandHandler implements ModifyRevenueConfigCommand {

    private final RevenueConfigRepository revenueConfigRepository;

    private final RevenueConfigHistoryRepository revenueConfigHistoryRepository;

    private final RevenueConfigValidator revenueConfigValidator;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws DomainException {

        RevenueConfig revenueConfig = this.revenueConfigRepository
                                          .findById(input.revenueConfigId())
                                          .orElseThrow(() -> new RevenueConfigException(
                                              RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                  input.revenueConfigId())));

        Optional<RevenueConfig> existingRevenueConfig = this.revenueConfigRepository
                                                            .findByTaxCodeId(input.taxCodeId())
                                                            .filter(existing -> !existing
                                                                                     .getRevenueConfigId()
                                                                                     .equals(
                                                                                         input.revenueConfigId()));
        if (existingRevenueConfig.isPresent()) {
            throw new RevenueConfigException(
                RevenueConfigErrors.TAX_CODE_ALREADY_REGISTERED.format(input.taxCodeId()));
        }

        this.revenueConfigValidator.validate(
            input.category(), input.responsibleMinistryId(), input.thirdPartyProviderId(),
            input.golPercentage(), input.ministryPercentage(), input.thirdPartyPercentage(),
            input.sendingDfspPercentage());

        this.revenueConfigHistoryRepository.save(new RevenueConfigHistory(revenueConfig));

        revenueConfig.update(
            input.taxCodeId(), input.taxCodeDescription(), input.category(),
            input.responsibleMinistryId(), input.thirdPartyProviderId(), input.golPercentage(),
            input.ministryPercentage(), input.thirdPartyPercentage(), input.sendingDfspPercentage(),
            input.updatedBy(), input.startDate());

        this.revenueConfigRepository.saveAndFlush(revenueConfig);

        return new Output(revenueConfig.getRevenueConfigId(), true);
    }

}
