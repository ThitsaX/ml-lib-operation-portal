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
import com.thitsaworks.operation_portal.core.participant.command.CreateParticipantCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateParticipant;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_MANAGEMENT)
public class CreateParticipantHandler
    extends OperationPortalAuditableUseCase<CreateParticipant.Input, CreateParticipant.Output>
    implements CreateParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(CreateParticipantHandler.class);

    private final CreateParticipantCommand createParticipantCommand;

    public CreateParticipantHandler(CreateInputAuditCommand createInputAuditCommand,
                                    CreateOutputAuditCommand createOutputAuditCommand,
                                    CreateExceptionAuditCommand createExceptionAuditCommand,
                                    ObjectMapper objectMapper,
                                    PrincipalCache principalCache,
                                    ActionAuthorizationManager actionAuthorizationManager,
                                    CreateParticipantCommand createParticipantCommand) {

        super(createInputAuditCommand,
              createOutputAuditCommand,
              createExceptionAuditCommand,
              objectMapper,
              principalCache,
              actionAuthorizationManager);

        this.createParticipantCommand = createParticipantCommand;
    }

    @Override
    public Output onExecute(Input input) throws DomainException {

        CreateParticipantCommand.Output output = this.createParticipantCommand.execute(
            new CreateParticipantCommand.Input(
                input.participantId(),
                input.participantName(),
                input.description(),
                input.address(),
                input.mobile(),
                input.status(),
                input.contactInfoList()
                     .stream()
                     .map(info -> new CreateParticipantCommand.Input.ContactInfo(info.name(),
                                                                                 info.position(),
                                                                                 info.email(),
                                                                                 info.mobile(),
                                                                                 info.contactType()))
                     .collect(Collectors.toList()),
                input.liquidityProfileInfoList()
                     .stream()
                     .map(info -> new CreateParticipantCommand.Input.LiquidityProfileInfo(
                         info.accountName(),
                         info.accountNumber(),
                         info.currency(),
                         info.status()))
                     .collect(Collectors.toList())));

        return new CreateParticipant.Output(output.created(), output.participantId());
    }

}
