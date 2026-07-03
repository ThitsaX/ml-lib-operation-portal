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

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.participant.data.LiquidityProfileData;
import com.thitsaworks.operation_portal.core.participant.query.LiquidityProfileQuery;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetLiquidityProfileList;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.LIQUIDITY_PROFILE)
public class GetLiquidityProfileListHandler
    extends OperationPortalUseCase<GetLiquidityProfileList.Input, GetLiquidityProfileList.Output>
    implements GetLiquidityProfileList {

    private static final Logger LOG = LoggerFactory.getLogger(GetLiquidityProfileListHandler.class);

    private final LiquidityProfileQuery liquidityProfileQuery;

    private final ParticipantQuery participantQuery;

    private final UserPermissionManager userPermissionManager;

    public GetLiquidityProfileListHandler(PrincipalCache principalCache,
                                          ActionAuthorizationManager actionAuthorizationManager,
                                          LiquidityProfileQuery liquidityProfileQuery,
                                          ParticipantQuery participantQuery,
                                          UserPermissionManager userPermissionManager) {

        super(principalCache, actionAuthorizationManager);

        this.liquidityProfileQuery = liquidityProfileQuery;
        this.participantQuery = participantQuery;
        this.userPermissionManager = userPermissionManager;

    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        if (this.userPermissionManager.isDfsp(currentUser.principalId())) {
            if (!this.userPermissionManager.isSameParticipant(
                new ParticipantId(currentUser.realmId().getId()), input.participantId())) {
                throw new IAMException(IAMErrors.UNAUTHORIZED_USER_ACCESS);
            }
        }

        var participant = this.participantQuery.get(input.participantId());

        List<LiquidityProfileData> liquidityProfileDataList = this.liquidityProfileQuery.getActiveLiquidityProfiles(
            participant.participantId());

        List<Output.LiquidityProfileInfo> liquidityProfileInfoList = new ArrayList<>();

        for (LiquidityProfileData liquidityProfileData : liquidityProfileDataList) {

            liquidityProfileInfoList.add(new Output.LiquidityProfileInfo(
                liquidityProfileData.liquidityProfileId(), liquidityProfileData.bankName(),
                liquidityProfileData.accountName(), liquidityProfileData.accountNumber(),
                liquidityProfileData.currency(), liquidityProfileData.isActive()));
        }

        return new Output(liquidityProfileInfoList);
    }

}
