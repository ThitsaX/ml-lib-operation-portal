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

import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.common.type.ParticipantStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.hub_services.data.HubParticipantData;
import com.thitsaworks.operation_portal.core.hub_services.query.HubParticipantQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.command.CreateParticipantCommand;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.SyncHubParticipantsToPortal;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_MANAGEMENT)
public class SyncHubParticipantsToPortalHandler
    extends OperationPortalUseCase<SyncHubParticipantsToPortal.Input, SyncHubParticipantsToPortal.Output>
    implements SyncHubParticipantsToPortal {

    private static final Logger LOG = LoggerFactory.getLogger(
        SyncHubParticipantsToPortalHandler.class);

    private final HubParticipantQuery hubParticipantQuery;

    private final ParticipantQuery participantQuery;

    private final CreateParticipantCommand createParticipantCommand;

    public SyncHubParticipantsToPortalHandler(PrincipalCache principalCache,
                                              ActionAuthorizationManager actionAuthorizationManager,
                                              HubParticipantQuery hubParticipantQuery,
                                              ParticipantQuery participantQuery,
                                              CreateParticipantCommand createParticipantCommand) {

        super(principalCache, actionAuthorizationManager);

        this.hubParticipantQuery = hubParticipantQuery;
        this.participantQuery = participantQuery;
        this.createParticipantCommand = createParticipantCommand;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        List<HubParticipantData> hubParticipantDataList = this.hubParticipantQuery.getParticipantList();

        Set<String> existingParticipantNames = this.participantQuery
                                                   .getAllParticipants()
                                                   .stream()
                                                   .map(pd -> pd.participantName().getValue())
                                                   .map(name -> name.toLowerCase(Locale.ROOT))
                                                   .collect(Collectors.toSet());

        List<CreatedParticipantInfo> createdParticipantInfoList = new ArrayList<>();

        for (HubParticipantData hubParticipant : hubParticipantDataList) {
            if (!existingParticipantNames.contains(hubParticipant.name().toLowerCase(Locale.ROOT))) {

                CreateParticipantCommand.Output output = this.createParticipantCommand.execute(
                    new CreateParticipantCommand.Input(
                        Integer.parseInt(hubParticipant.participantId()),
                        new ParticipantName(hubParticipant.name()), hubParticipant.description(),
                        null, null, ParticipantStatus.ACTIVE,

                        null, null));

                createdParticipantInfoList.add(new CreatedParticipantInfo(
                    output.participantId().getId().toString(), hubParticipant.name(),
                    hubParticipant.description()));

            }
        }

        try {
            LOG.info("Created participants : [{}]", createdParticipantInfoList);

        } catch (Exception e) {
            LOG.info("Something went wrong: {}", e.getMessage());
        }

        return new Output(true);
    }

    record CreatedParticipantInfo(String participantId, String name, String description) { }

}
