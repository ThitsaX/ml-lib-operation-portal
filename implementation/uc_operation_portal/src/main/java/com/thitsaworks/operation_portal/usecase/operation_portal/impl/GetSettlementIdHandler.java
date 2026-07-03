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
import com.thitsaworks.operation_portal.reporting.report.query.GetSettlementIdsQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSettlementId;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.SETTLEMENT_CORE_OPERATIONS)
public class GetSettlementIdHandler
    extends OperationPortalUseCase<GetSettlementId.Input, GetSettlementId.Output>
    implements GetSettlementId {

    private static final Logger LOG = LoggerFactory.getLogger(GetSettlementIdHandler.class);

    private final GetSettlementIdsQuery getSettlementIdsQuery;

    public GetSettlementIdHandler(PrincipalCache principalCache,
                                  ActionAuthorizationManager actionAuthorizationManager,
                                  GetSettlementIdsQuery getSettlementIdsQuery) {

        super(principalCache, actionAuthorizationManager);

        this.getSettlementIdsQuery = getSettlementIdsQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        GetSettlementIdsQuery.Output output = this.getSettlementIdsQuery.execute(
            new GetSettlementIdsQuery.Input(
                Timestamp.from(input.startDate()), Timestamp.from(input.endDate()), input.dfspId(),
                input.timezoneOffset()));

        List<SettlementIdData> settlementIdData = new ArrayList<>();

        for (SettlementIdData data : output.settlementId()) {

            settlementIdData.add(new SettlementIdData(data.getSettlementId()));
        }

        return new GetSettlementId.Output(settlementIdData);
    }

}
