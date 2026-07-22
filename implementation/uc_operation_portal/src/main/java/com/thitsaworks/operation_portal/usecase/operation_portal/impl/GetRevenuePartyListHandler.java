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
import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenuePartyList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_PARTY)
public class GetRevenuePartyListHandler
    extends OperationPortalUseCase<GetRevenuePartyList.Input, GetRevenuePartyList.Output>
    implements GetRevenuePartyList {

    private final RevenuePartyQuery revenuePartyQuery;

    public GetRevenuePartyListHandler(PrincipalCache principalCache,
                                      ActionAuthorizationManager actionAuthorizationManager,
                                      RevenuePartyQuery revenuePartyQuery) {

        super(principalCache, actionAuthorizationManager);

        this.revenuePartyQuery = revenuePartyQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        List<RevenuePartyData> revenueParties = this.revenuePartyQuery.getRevenueParties();

        return new Output(revenueParties);
    }

}
