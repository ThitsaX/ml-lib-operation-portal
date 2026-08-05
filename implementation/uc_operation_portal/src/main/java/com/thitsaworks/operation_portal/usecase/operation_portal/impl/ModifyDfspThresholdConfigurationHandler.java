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
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.notification.command.ModifyThresholdConfigurationCommand;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdConfigurationData;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyDfspThresholdConfiguration;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.SchedulerEngine;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class ModifyDfspThresholdConfigurationHandler
    extends OperationPortalAuditableUseCase<ModifyDfspThresholdConfiguration.Input,
                                             ModifyDfspThresholdConfiguration.Output>
    implements ModifyDfspThresholdConfiguration {

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    private final ModifyThresholdConfigurationCommand modifyThresholdConfigurationCommand;

    private final UserPermissionManager userPermissionManager;

    private final ParticipantQuery participantQuery;

    private final SchedulerEngine schedulerEngine;

    public ModifyDfspThresholdConfigurationHandler(
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ObjectMapper objectMapper,
        PrincipalCache principalCache,
        ActionAuthorizationManager actionAuthorizationManager,
        ThresholdConfigurationQuery thresholdConfigurationQuery,
        ModifyThresholdConfigurationCommand modifyThresholdConfigurationCommand,
        UserPermissionManager userPermissionManager,
        ParticipantQuery participantQuery,
        SchedulerEngine schedulerEngine) {

        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
              objectMapper, principalCache, actionAuthorizationManager);

        this.thresholdConfigurationQuery = thresholdConfigurationQuery;
        this.modifyThresholdConfigurationCommand = modifyThresholdConfigurationCommand;
        this.userPermissionManager = userPermissionManager;
        this.participantQuery = participantQuery;
        this.schedulerEngine = schedulerEngine;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        ThresholdConfigurationId configurationId = new ThresholdConfigurationId(input.id());

        ThresholdConfigurationData configuration = this.thresholdConfigurationQuery
            .get(configurationId)
            .orElseThrow(() -> new InputException(new ErrorMessage(
                "THRESHOLD_CONFIGURATION_NOT_FOUND",
                "Threshold configuration was not found.")));

        this.validateDfspAccess(configuration);

        ModifyThresholdConfigurationCommand.Output output =
            this.modifyThresholdConfigurationCommand.execute(
                new ModifyThresholdConfigurationCommand.Input(
                    configurationId,
                    input.thresholdEnabled(),
                    input.status() == null ? NdcConfigurationStatus.ACTIVE : input.status(),
                    input.updatedBy()));

        this.schedulerEngine.refreshAllActive();

        return new Output(output.thresholdConfigurationId(), output.modified());
    }

    private void validateDfspAccess(ThresholdConfigurationData configuration)
        throws DomainException {

        if (configuration.scopeType() != ThresholdScopeType.DFSP) {
            throw accessDenied();
        }

        var currentUser = this.userPermissionManager.getCurrentUser();

        if (!this.userPermissionManager.isDfsp(currentUser.principalId())) {
            return;
        }

        var currentParticipant = this.participantQuery.get(
            new ParticipantId(currentUser.realmId().getId()));

        String currentDfsp = currentParticipant.participantName().getValue();

        if (!Objects.equals(currentDfsp, configuration.dfspId())) {
            throw accessDenied();
        }
    }

    private IAMException accessDenied() {

        return new IAMException(
            IAMErrors.PERMISSION_DENIED.format("ModifyDfspThresholdConfiguration"));
    }
}
