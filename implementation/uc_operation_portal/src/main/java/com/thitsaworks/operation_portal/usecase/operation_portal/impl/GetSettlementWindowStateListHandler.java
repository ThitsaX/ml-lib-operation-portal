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
import com.thitsaworks.operation_portal.core.hub_services.data.SettlementWindowStateData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetSettlementWindowStateQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSettlementWindowStateList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.SETTLEMENT_CORE_OPERATIONS)
public class GetSettlementWindowStateListHandler
    extends OperationPortalUseCase<GetSettlementWindowStateList.Input, GetSettlementWindowStateList.Output>
    implements GetSettlementWindowStateList {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        GetSettlementWindowStateListHandler.class);

    private final GetSettlementWindowStateQuery settlementWindowStateQuery;

    public GetSettlementWindowStateListHandler(PrincipalCache principalCache,
                                               ActionAuthorizationManager actionAuthorizationManager,
                                               GetSettlementWindowStateQuery settlementWindowStateQuery) {

        super(principalCache, actionAuthorizationManager);
        this.settlementWindowStateQuery = settlementWindowStateQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, ConnectException {

        var output = this.settlementWindowStateQuery.execute(
            new GetSettlementWindowStateQuery.Input());

        List<Output.SettlementWindowStateData> settlementWindowStates = new ArrayList<>();

        for (SettlementWindowStateData data : output.settlementWindowStates()) {
            String state = data.enumeration();
            if (!"FAILED".equalsIgnoreCase(state)) {
                if ("PENDING_SETTLEMENT".equalsIgnoreCase(state)) {
                    state = "PENDING";
                }
                settlementWindowStates.add(
                    new Output.SettlementWindowStateData(data.settlementWindowStateId(), state));
            }
        }
        return new Output(settlementWindowStates);
    }

}
