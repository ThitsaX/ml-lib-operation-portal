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
import com.thitsaworks.operation_portal.core.hub_services.data.TransferStateData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetTransferStatesQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetTransferStateList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.TRANSFER_OPERATIONS)
public class GetTransferStateListHandler
    extends OperationPortalUseCase<GetTransferStateList.Input, GetTransferStateList.Output>
    implements GetTransferStateList {

    private static final Logger LOG = LoggerFactory.getLogger(GetTransferStateListHandler.class);

    private final GetTransferStatesQuery getTransferStatesQuery;

    public GetTransferStateListHandler(PrincipalCache principalCache,
                                       ActionAuthorizationManager actionAuthorizationManager,
                                       GetTransferStatesQuery getTransferStatesQuery) {

        super(principalCache, actionAuthorizationManager);

        this.getTransferStatesQuery = getTransferStatesQuery;

    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        GetTransferStatesQuery.Output output = this.getTransferStatesQuery.execute(
            new GetTransferStatesQuery.Input());

        List<TransferStateData> transferStateDataList = new ArrayList<>();

        for (TransferStateData data : output.getTransferStateDataList()) {

            transferStateDataList.add(
                new TransferStateData(data.getTransferStateId(), data.getTransferState()));
        }

        return new GetTransferStateList.Output(transferStateDataList);
    }

}
