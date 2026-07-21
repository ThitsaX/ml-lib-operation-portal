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
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.core.revenue_party.command.ModifyRevenuePartyCommand;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenueParty;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_PARTY)
public class ModifyRevenuePartyHandler
    extends OperationPortalAuditableUseCase<ModifyRevenueParty.Input, ModifyRevenueParty.Output>
    implements ModifyRevenueParty {

    private final ModifyRevenuePartyCommand modifyRevenuePartyCommand;
    private final RevenuePartyQuery revenuePartyQuery;
    private final UserPermissionManager userPermissionManager;

    public ModifyRevenuePartyHandler(CreateInputAuditCommand createInputAuditCommand,
                                     CreateOutputAuditCommand createOutputAuditCommand,
                                     CreateExceptionAuditCommand createExceptionAuditCommand,
                                     ObjectMapper objectMapper,
                                     PrincipalCache principalCache,
                                     ActionAuthorizationManager actionAuthorizationManager,
                                     ModifyRevenuePartyCommand modifyRevenuePartyCommand,
                                     RevenuePartyQuery revenuePartyQuery,
                                     UserPermissionManager userPermissionManager) {

        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
              objectMapper, principalCache, actionAuthorizationManager);

        this.modifyRevenuePartyCommand = modifyRevenuePartyCommand;
        this.revenuePartyQuery = revenuePartyQuery;
        this.userPermissionManager = userPermissionManager;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var beforeValue = this.revenuePartyQuery.get(input.revenuePartyId());
        var currentUser = this.userPermissionManager.getCurrentUser();
        var output = this.modifyRevenuePartyCommand.execute(new ModifyRevenuePartyCommand.Input(
            input.revenuePartyId(), input.partyCode(), input.partyName(), input.partyType(),
            input.description(), new UserId(currentUser.principalId().getId())));

        return new Output(output.modified(), output.revenuePartyId(), beforeValue,
                          this.revenuePartyQuery.get(output.revenuePartyId()));
    }

}

