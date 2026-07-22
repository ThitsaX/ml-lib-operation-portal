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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.notification.command.ModifyThresholdConfigurationCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyThresholdConfiguration;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.SchedulerEngine;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class ModifyThresholdConfigurationHandler
    extends OperationPortalAuditableUseCase<ModifyThresholdConfiguration.Input,
        ModifyThresholdConfiguration.Output>
    implements ModifyThresholdConfiguration {

    private final ModifyThresholdConfigurationCommand modifyThresholdConfigurationCommand;

    private final SchedulerEngine schedulerEngine;

    public ModifyThresholdConfigurationHandler(CreateInputAuditCommand createInputAuditCommand, CreateOutputAuditCommand createOutputAuditCommand, CreateExceptionAuditCommand createExceptionAuditCommand, ObjectMapper objectMapper, PrincipalCache principalCache, ActionAuthorizationManager actionAuthorizationManager, ModifyThresholdConfigurationCommand modifyThresholdConfigurationCommand, SchedulerEngine schedulerEngine) {
        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand, objectMapper, principalCache, actionAuthorizationManager);
        this.modifyThresholdConfigurationCommand = modifyThresholdConfigurationCommand;
        this.schedulerEngine = schedulerEngine;
    }


    @Override
    protected Output onExecute(Input input) throws DomainException {

        ModifyThresholdConfigurationCommand.Output output = this.modifyThresholdConfigurationCommand.execute(
            new ModifyThresholdConfigurationCommand.Input(
                new ThresholdConfigurationId(input.id()),
                input.thresholdEnabled(),
                input.status() == null ? NdcConfigurationStatus.ACTIVE : input.status(),
                input.updatedBy()));

        this.schedulerEngine.refreshAllActive();

        return new Output(output.thresholdConfigurationId(), output.modified());
    }

}
