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
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.command.ResetPasswordCommand;
import com.thitsaworks.operation_portal.core.participant.data.UserData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.query.UserQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ResetCurrentPassword;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.AUTHENTICATION_AND_ACCOUNT_SECURITY)
public class ResetCurrentPasswordHandler
    extends OperationPortalAuditableUseCase<ResetCurrentPassword.Input, ResetCurrentPassword.Output>
    implements ResetCurrentPassword {

    private static final Logger LOG = LoggerFactory.getLogger(ResetCurrentPasswordHandler.class);

    private final ResetPasswordCommand resetPasswordCommand;

    private final UserQuery userQuery;

    public ResetCurrentPasswordHandler(CreateInputAuditCommand createInputAuditCommand,
                                       CreateOutputAuditCommand createOutputAuditCommand,
                                       CreateExceptionAuditCommand createExceptionAuditCommand,
                                       ObjectMapper objectMapper,
                                       PrincipalCache principalCache,
                                       ActionAuthorizationManager actionAuthorizationManager,
                                       ResetPasswordCommand resetPasswordCommand,
                                       UserQuery userQuery) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.resetPasswordCommand = resetPasswordCommand;
        this.userQuery = userQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        UserData userData = this.userQuery.get(input.email());

        if (userData.userId() == null) {

            throw new ParticipantException(
                ParticipantErrors.EMAIL_NOT_FOUND.format(input.email().getValue()));
        }

        ResetPasswordCommand.Output resetPasswordOutput = this.resetPasswordCommand.execute(
            new ResetPasswordCommand.Input(
                new PrincipalId(userData.userId().getId()),
                input.password().getValue()));

        return new Output(
            resetPasswordOutput.accessKey(), resetPasswordOutput.secretKey(),
            resetPasswordOutput.updated());
    }

}
