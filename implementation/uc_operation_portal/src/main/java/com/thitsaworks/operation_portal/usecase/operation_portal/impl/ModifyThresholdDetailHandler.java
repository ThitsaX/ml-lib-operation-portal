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

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.notification.command.ModifyThresholdDetailCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyThresholdDetail;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class ModifyThresholdDetailHandler
    extends OperationPortalUseCase<ModifyThresholdDetail.Input, ModifyThresholdDetail.Output>
    implements ModifyThresholdDetail {

    private final ModifyThresholdDetailCommand modifyThresholdDetailCommand;

    private final ThresholdDetailCurrencyValidator currencyValidator;

    public ModifyThresholdDetailHandler(PrincipalCache principalCache,
                                        ActionAuthorizationManager actionAuthorizationManager,
                                        ModifyThresholdDetailCommand modifyThresholdDetailCommand,
                                        ThresholdDetailCurrencyValidator currencyValidator) {

        super(principalCache, actionAuthorizationManager);
        this.modifyThresholdDetailCommand = modifyThresholdDetailCommand;
        this.currencyValidator = currencyValidator;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        ThresholdDetailId thresholdDetailId = new ThresholdDetailId(input.id());
        String currency = this.currencyValidator.validateForDetail(
            thresholdDetailId, input.currency());

        ModifyThresholdDetailCommand.Output output = this.modifyThresholdDetailCommand.execute(
            new ModifyThresholdDetailCommand.Input(
                thresholdDetailId,
                currency,
                input.visualConfig(),
                input.ndcConfig(),
                input.status(),
                input.updatedBy()));

        return new Output(output.thresholdDetailId(), output.modified());
    }
}
