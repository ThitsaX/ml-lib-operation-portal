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

import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetParticipantList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_MANAGEMENT)
public class GetParticipantListHandler
    extends OperationPortalUseCase<GetParticipantList.Input, GetParticipantList.Output>
    implements GetParticipantList {

    private static final Logger LOG = LoggerFactory.getLogger(GetParticipantListHandler.class);

    private final ParticipantQuery participantQuery;

    public GetParticipantListHandler(PrincipalCache principalCache,
                                     ActionAuthorizationManager actionAuthorizationManager,
                                     ParticipantQuery participantQuery) {

        super(principalCache, actionAuthorizationManager);

        this.participantQuery = participantQuery;
    }

    @Override
    public GetParticipantList.Output onExecute(GetParticipantList.Input input)
        throws DomainException {

        List<ParticipantData> participantDataList = this.participantQuery.getAllParticipants();

        List<GetParticipantList.Output.ParticipantInfo> participantInfoList = new ArrayList<>();

        for (ParticipantData participantData : participantDataList) {

            if (participantData.participantName() != null &&
                    !participantData.participantName().getValue().toLowerCase().contains("hub")) {

                participantInfoList.add(new GetParticipantList.Output.ParticipantInfo(
                    participantData.participantId(), participantData.dfspId(),
                    participantData.participantName().getValue(),

                    participantData.description(), participantData.address(),
                    participantData.mobile(), participantData.logoFileType(),
                    participantData.logo(), Instant.ofEpochSecond(participantData.createdDate())));
            }
        }

        return new GetParticipantList.Output(participantInfoList);
    }

}
