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
import com.thitsaworks.operation_portal.core.revenue_party.command.ChangeRevenuePartyStatusCommand;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenuePartyStatus;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.RevenuePartyDataMapper;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_PARTY)
public class ModifyRevenuePartyStatusHandler
    extends OperationPortalAuditableUseCase<ModifyRevenuePartyStatus.Input, ModifyRevenuePartyStatus.Output>
    implements ModifyRevenuePartyStatus {

    private final ChangeRevenuePartyStatusCommand changeRevenuePartyStatusCommand;
    private final RevenuePartyQuery revenuePartyQuery;
    private final UserPermissionManager userPermissionManager;
    private final RevenuePartyDataMapper revenuePartyDataMapper;

    public ModifyRevenuePartyStatusHandler(CreateInputAuditCommand createInputAuditCommand,
                                           CreateOutputAuditCommand createOutputAuditCommand,
                                           CreateExceptionAuditCommand createExceptionAuditCommand,
                                           ObjectMapper objectMapper,
                                           PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           ChangeRevenuePartyStatusCommand changeRevenuePartyStatusCommand,
                                           RevenuePartyQuery revenuePartyQuery,
                                           UserPermissionManager userPermissionManager,
                                           RevenuePartyDataMapper revenuePartyDataMapper) {

        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
              objectMapper, principalCache, actionAuthorizationManager);

        this.changeRevenuePartyStatusCommand = changeRevenuePartyStatusCommand;
        this.revenuePartyQuery = revenuePartyQuery;
        this.userPermissionManager = userPermissionManager;
        this.revenuePartyDataMapper = revenuePartyDataMapper;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var beforeValue = this.revenuePartyDataMapper.withUserEmails(
            this.revenuePartyQuery.get(input.revenuePartyId()));
        var currentUser = this.userPermissionManager.getCurrentUser();
        var output = this.changeRevenuePartyStatusCommand.execute(new ChangeRevenuePartyStatusCommand.Input(
            input.revenuePartyId(), input.status(), new UserId(currentUser.principalId().getId())));

        return new Output(output.modified(), output.revenuePartyId(), beforeValue,
                          this.revenuePartyDataMapper.withUserEmails(
                              this.revenuePartyQuery.get(output.revenuePartyId())));
    }

}

