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
import com.thitsaworks.operation_portal.core.hub_services.SettlementHubClient;
import com.thitsaworks.operation_portal.core.hub_services.api.GetSettlementWindows;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSettlementWindowsList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.SETTLEMENT_CORE_OPERATIONS)
public class GetSettlementWindowsListHandler
    extends OperationPortalUseCase<GetSettlementWindowsList.Input, GetSettlementWindowsList.Output>
    implements GetSettlementWindowsList {

    private static final Logger logger = LoggerFactory.getLogger(
        GetSettlementWindowsListHandler.class);

    private final SettlementHubClient settlementHubClient;

    @Autowired
    public GetSettlementWindowsListHandler(PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           SettlementHubClient settlementHubClient) {

        super(principalCache, actionAuthorizationManager);
        this.settlementHubClient = settlementHubClient;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, ConnectException {

        GetSettlementWindows.Request request = new GetSettlementWindows.Request();

        List<GetSettlementWindows.SettlementWindow> response = this.settlementHubClient.getSettlementWindowsList(
            input.fromDate(), input.toDate(), input.currency(), input.state(),
            input.participantId(), request);

        return new Output(response);
    }

}