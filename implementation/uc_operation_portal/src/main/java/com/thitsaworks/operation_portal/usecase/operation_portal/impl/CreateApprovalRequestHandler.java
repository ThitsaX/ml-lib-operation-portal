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
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.approval.command.CreateApprovalRequestCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateApprovalRequest;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.APPROVAL_WORKFLOW)
public class CreateApprovalRequestHandler
    extends OperationPortalAuditableUseCase<CreateApprovalRequest.Input, CreateApprovalRequest.Output>
    implements CreateApprovalRequest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateApprovalRequestHandler.class);

    private final CreateApprovalRequestCommand createApprovalRequestCommand;

    public CreateApprovalRequestHandler(CreateInputAuditCommand createInputAuditCommand,
                                        CreateOutputAuditCommand createOutputAuditCommand,
                                        CreateExceptionAuditCommand createExceptionAuditCommand,
                                        ObjectMapper objectMapper,
                                        PrincipalCache principalCache,
                                        CreateApprovalRequestCommand createApprovalRequestCommand,
                                        ActionAuthorizationManager actionAuthorizationManager) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.createApprovalRequestCommand = createApprovalRequestCommand;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var output = this.createApprovalRequestCommand.execute(
            new CreateApprovalRequestCommand.Input(
                input.requestedAction(), input.participant(), input.participantCurrency(),
                input.participantSettlementCurrencyId(), input.participantPositionCurrencyId(),
                input.amount(), input.requestedBy()));

        return new Output(output.approvalRequestId());
    }

}
