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
import com.thitsaworks.operation_portal.core.participant.data.ContactData;
import com.thitsaworks.operation_portal.core.participant.data.LiquidityProfileData;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import com.thitsaworks.operation_portal.core.participant.query.ContactQuery;
import com.thitsaworks.operation_portal.core.participant.query.LiquidityProfileQuery;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetParticipant;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_MANAGEMENT)
public class GetParticipantHandler
    extends OperationPortalUseCase<GetParticipant.Input, GetParticipant.Output>
    implements GetParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(GetParticipantHandler.class);

    private final ParticipantQuery participantQuery;

    private final ContactQuery contactQuery;

    private final LiquidityProfileQuery liquidityProfileQuery;

    public GetParticipantHandler(PrincipalCache principalCache,
                                 ActionAuthorizationManager actionAuthorizationManager,
                                 ParticipantQuery participantQuery,
                                 ContactQuery contactQuery,
                                 LiquidityProfileQuery liquidityProfileQuery) {

        super(principalCache, actionAuthorizationManager);

        this.participantQuery = participantQuery;
        this.contactQuery = contactQuery;
        this.liquidityProfileQuery = liquidityProfileQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        ParticipantData participantData = this.participantQuery.get(input.participantId());

        List<ContactData> contactDataList = this.contactQuery.getContacts(input.participantId());

        List<Output.ContactInfo> contactInfoList = new ArrayList<>();

        for (ContactData contactData : contactDataList) {

            contactInfoList.add(new Output.ContactInfo(
                contactData.contactId(), contactData.name(), contactData.position(),
                contactData.email(), contactData.mobile(), contactData.contactType().name()));
        }

        List<LiquidityProfileData> liquidityProfileDataList = this.liquidityProfileQuery.getLiquidityProfiles(
            input.participantId());

        List<Output.LiquidityProfileInfo> liquidityProfileInfoList = new ArrayList<>();

        for (LiquidityProfileData liquidityProfileData : liquidityProfileDataList) {

            liquidityProfileInfoList.add(new Output.LiquidityProfileInfo(
                liquidityProfileData.liquidityProfileId(), liquidityProfileData.bankName(),
                liquidityProfileData.accountName(), liquidityProfileData.accountNumber(),
                liquidityProfileData.currency(), liquidityProfileData.isActive()));
        }

        return new Output(
            participantData.participantId(), participantData.participantName().getValue(),
            participantData.description(), participantData.address(), participantData.mobile(),
            participantData.logoFileType(), participantData.logo(),
            Instant.ofEpochSecond(participantData.createdDate()), contactInfoList,
            liquidityProfileInfoList);
    }

}
