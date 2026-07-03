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
import com.thitsaworks.operation_portal.reporting.report.domain.data.SettlementIdData;
import com.thitsaworks.operation_portal.reporting.report.query.GetSettlementIdsWithParentParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSettlementIdWithParentParticipant;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.SETTLEMENT_CORE_OPERATIONS)
public class GetSettlementIdWithParentParticipantHandler
    extends OperationPortalUseCase<GetSettlementIdWithParentParticipant.Input, GetSettlementIdWithParentParticipant.Output>
    implements GetSettlementIdWithParentParticipant {

    private final GetSettlementIdsWithParentParticipantQuery getSettlementIdsWithParentParticipantQuery;

    public GetSettlementIdWithParentParticipantHandler(PrincipalCache principalCache,
                                                       ActionAuthorizationManager actionAuthorizationManager,
                                                       GetSettlementIdsWithParentParticipantQuery getSettlementIdsWithParentParticipantQuery) {

        super(principalCache, actionAuthorizationManager);
        this.getSettlementIdsWithParentParticipantQuery = getSettlementIdsWithParentParticipantQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        GetSettlementIdsWithParentParticipantQuery.Output output = this.getSettlementIdsWithParentParticipantQuery.execute(
            new GetSettlementIdsWithParentParticipantQuery.Input(
                Timestamp.from(input.startDate()), Timestamp.from(input.endDate()), input.dfspId(),
                input.timezoneOffset()));

        List<SettlementIdData> settlementIdData = new ArrayList<>();
        for (SettlementIdData data : output.settlementId()) {
            settlementIdData.add(new SettlementIdData(data.getSettlementId()));
        }

        return new Output(settlementIdData);
    }

}
