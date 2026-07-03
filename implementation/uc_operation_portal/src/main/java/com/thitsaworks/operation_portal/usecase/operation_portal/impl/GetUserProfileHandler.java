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

import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.data.PrincipalData;
import com.thitsaworks.operation_portal.core.iam.data.RoleData;
import com.thitsaworks.operation_portal.core.iam.query.IAMQuery;
import com.thitsaworks.operation_portal.core.participant.cache.ParticipantCache;
import com.thitsaworks.operation_portal.core.participant.cache.UserCache;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import com.thitsaworks.operation_portal.core.participant.data.UserData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetUserProfile;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.AUTHENTICATION_AND_ACCOUNT_SECURITY)
public class GetUserProfileHandler
    extends OperationPortalUseCase<GetUserProfile.Input, GetUserProfile.Output>
    implements GetUserProfile {

    private static final Logger LOG = LoggerFactory.getLogger(GetUserProfileHandler.class);

    private final ParticipantCache participantCache;

    private final UserCache userCache;

    private final PrincipalCache principalCache;

    private final IAMQuery iamQuery;

    public GetUserProfileHandler(PrincipalCache principalCache,
                                 ActionAuthorizationManager actionAuthorizationManager,
                                 ParticipantCache participantCache,
                                 UserCache userCache,
                                 IAMQuery iamQuery) {

        super(principalCache, actionAuthorizationManager);

        this.participantCache = participantCache;
        this.userCache = userCache;
        this.principalCache = principalCache;
        this.iamQuery = iamQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        UserData userData = this.userCache.get(input.userId());

        PrincipalData principalData = this.principalCache.get(
            new PrincipalId(input.userId().getId()));

        if (userData == null || principalData == null) {

            throw new ParticipantException(
                ParticipantErrors.USER_NOT_FOUND.format(input.userId().getId().toString()));
        }

        ParticipantData participantData = this.participantCache.get(userData.participantId());

        if (participantData == null) {

            throw new ParticipantException(ParticipantErrors.PARTICIPANT_NOT_FOUND.format(
                userData.participantId().getId().toString()));
        }

        var roleList = this.iamQuery
                           .getRoleListByPrincipal(principalData.principalId())
                           .stream()
                           .map(RoleData::name)
                           .toList();

        var permittedMenuAndActionList = this.iamQuery.getMenuAndActionListByUserId(
            principalData.principalId());

        return new Output(
            userData.userId(), userData.name(), userData.email(), userData.firstName(),
            userData.lastName(), userData.jobTitle(), userData.participantId(),
            userData.createdDate(), participantData.participantName().getValue(),
            participantData.description(), participantData.logoFileType(), participantData.logo(),
            roleList, permittedMenuAndActionList);

    }

}
