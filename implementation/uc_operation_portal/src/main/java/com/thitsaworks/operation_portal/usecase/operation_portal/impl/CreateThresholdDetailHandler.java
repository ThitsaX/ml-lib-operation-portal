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
package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.notification.command.CreateThresholdDetailCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateThresholdDetail;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class CreateThresholdDetailHandler
    extends OperationPortalUseCase<CreateThresholdDetail.Input, CreateThresholdDetail.Output>
    implements CreateThresholdDetail {

    private final CreateThresholdDetailCommand createThresholdDetailCommand;

    private final ThresholdDetailCurrencyValidator currencyValidator;

    public CreateThresholdDetailHandler(PrincipalCache principalCache,
                                        ActionAuthorizationManager actionAuthorizationManager,
                                        CreateThresholdDetailCommand createThresholdDetailCommand,
                                        ThresholdDetailCurrencyValidator currencyValidator) {

        super(principalCache, actionAuthorizationManager);
        this.createThresholdDetailCommand = createThresholdDetailCommand;
        this.currencyValidator = currencyValidator;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        ThresholdConfigurationId thresholdConfigurationId =
            new ThresholdConfigurationId(input.thresholdConfigurationId());
        String currency = this.currencyValidator.validateForConfiguration(
            thresholdConfigurationId, input.currency());

        CreateThresholdDetailCommand.Output output = this.createThresholdDetailCommand.execute(
            new CreateThresholdDetailCommand.Input(
                thresholdConfigurationId,
                currency,
                input.visualConfig(),
                input.ndcConfig(),
                input.createdBy()));

        return new Output(output.thresholdDetailId());
    }
}
