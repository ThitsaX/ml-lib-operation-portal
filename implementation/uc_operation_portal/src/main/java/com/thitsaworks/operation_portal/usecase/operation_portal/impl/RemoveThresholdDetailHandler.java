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
import com.thitsaworks.operation_portal.core.notification.command.RemoveThresholdDetailCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.RemoveThresholdDetail;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.SchedulerEngine;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class RemoveThresholdDetailHandler
    extends OperationPortalUseCase<RemoveThresholdDetail.Input, RemoveThresholdDetail.Output>
    implements RemoveThresholdDetail {

    private final RemoveThresholdDetailCommand removeThresholdDetailCommand;

    private final SchedulerEngine schedulerEngine;

    public RemoveThresholdDetailHandler(PrincipalCache principalCache,
                                        ActionAuthorizationManager actionAuthorizationManager,
                                        RemoveThresholdDetailCommand removeThresholdDetailCommand,
                                        SchedulerEngine schedulerEngine) {

        super(principalCache, actionAuthorizationManager);
        this.removeThresholdDetailCommand = removeThresholdDetailCommand;
        this.schedulerEngine = schedulerEngine;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        RemoveThresholdDetailCommand.Output output = this.removeThresholdDetailCommand.execute(
            new RemoveThresholdDetailCommand.Input(
                new ThresholdDetailId(input.id()),
                input.updatedBy()));

        this.schedulerEngine.refreshAllActive();

        return new Output(output.thresholdDetailId(), output.removed());
    }
}
