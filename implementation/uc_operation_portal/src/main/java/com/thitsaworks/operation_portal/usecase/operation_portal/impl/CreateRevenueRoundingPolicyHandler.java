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
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_config.command.CreateRevenueRoundingPolicyCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateRevenueRoundingPolicy;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_CONFIG)
public class CreateRevenueRoundingPolicyHandler
    extends OperationPortalAuditableUseCase<CreateRevenueRoundingPolicy.Input,
                                               CreateRevenueRoundingPolicy.Output>
    implements CreateRevenueRoundingPolicy {

    private final CreateRevenueRoundingPolicyCommand createRevenueRoundingPolicyCommand;

    private final UserPermissionManager userPermissionManager;

    public CreateRevenueRoundingPolicyHandler(
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ObjectMapper objectMapper,
        PrincipalCache principalCache,
        ActionAuthorizationManager actionAuthorizationManager,
        CreateRevenueRoundingPolicyCommand createRevenueRoundingPolicyCommand,
        UserPermissionManager userPermissionManager) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.createRevenueRoundingPolicyCommand = createRevenueRoundingPolicyCommand;
        this.userPermissionManager = userPermissionManager;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        var output = this.createRevenueRoundingPolicyCommand.execute(
            new CreateRevenueRoundingPolicyCommand.Input(
                input.roundingMode(), input.remainderRecipient(),
                new UserId(currentUser.principalId().getId())));

        return new Output(
            true, output.revenueRoundingPolicyId());
    }
}
