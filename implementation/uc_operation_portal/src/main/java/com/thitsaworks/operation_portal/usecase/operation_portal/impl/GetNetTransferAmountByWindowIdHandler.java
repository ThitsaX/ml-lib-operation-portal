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
import com.thitsaworks.operation_portal.core.hub_services.data.WindowInfoData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNetTransferAmountByWindowIdQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetNetTransferAmountByWindowId;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.SETTLEMENT_CORE_OPERATIONS)
public class GetNetTransferAmountByWindowIdHandler
    extends OperationPortalUseCase<GetNetTransferAmountByWindowId.Input, GetNetTransferAmountByWindowId.Output>
    implements GetNetTransferAmountByWindowId {

    private static final Logger LOG = LoggerFactory.getLogger(
        GetNetTransferAmountByWindowIdHandler.class);

    private final GetNetTransferAmountByWindowIdQuery getNetTrasferAmountByWindowIdQuery;

    public GetNetTransferAmountByWindowIdHandler(PrincipalCache principalCache,
                                                 ActionAuthorizationManager actionAuthorizationManager,
                                                 GetNetTransferAmountByWindowIdQuery getNetTransferAmountByWindowIdQuery) {

        super(principalCache, actionAuthorizationManager);

        this.getNetTrasferAmountByWindowIdQuery = getNetTransferAmountByWindowIdQuery;

    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        GetNetTransferAmountByWindowIdQuery.Output output = this.getNetTrasferAmountByWindowIdQuery.execute(
            new GetNetTransferAmountByWindowIdQuery.Input(input.settlementWindowId()));

        List<GetNetTransferAmountByWindowId.Detail> details = new ArrayList<>();

        for (WindowInfoData windowInfo : output.getWindowInfoList()) {

            GetNetTransferAmountByWindowId.Detail detail = new GetNetTransferAmountByWindowId.Detail(
                windowInfo.getDfspName(), windowInfo.getDebit(), windowInfo.getCredit(),
                windowInfo.getCurrencyId());

            details.add(detail);
        }

        String windowOpenedDate = output.getWindowInfoList().isEmpty() ? null :
                                      output.getWindowInfoList().get(0).getWindowOpenedDate();

        String windowClosedDate = output.getWindowInfoList().isEmpty() ? null :
                                      output.getWindowInfoList().get(0).getWindowClosedDate();

        return new GetNetTransferAmountByWindowId.Output(
            input.settlementWindowId(),
            windowOpenedDate, windowClosedDate, details);

    }

}
