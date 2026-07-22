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

import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_config.data.RevenueConfigData;
import com.thitsaworks.operation_portal.core.revenue_config.query.RevenueConfigQuery;
import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenueConfigById;
import com.thitsaworks.operation_portal.usecase.util.Utility;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_CONFIG)
public class GetRevenueConfigByIdHandler
    extends OperationPortalUseCase<GetRevenueConfigById.Input, GetRevenueConfigById.Output>
    implements GetRevenueConfigById {

    private final RevenueConfigQuery revenueConfigQuery;

    private final RevenuePartyQuery revenuePartyQuery;

    private final Utility utility;

    public GetRevenueConfigByIdHandler(PrincipalCache principalCache,
                                       ActionAuthorizationManager actionAuthorizationManager,
                                       RevenueConfigQuery revenueConfigQuery,
                                       RevenuePartyQuery revenuePartyQuery,
                                       Utility utility) {

        super(principalCache, actionAuthorizationManager);
        this.revenueConfigQuery = revenueConfigQuery;
        this.revenuePartyQuery = revenuePartyQuery;
        this.utility = utility;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        return new Output(this.map(this.revenueConfigQuery.get(input.revenueConfigId())));
    }

    private GetRevenueConfigById.RevenueConfig map(RevenueConfigData data) {

        return new GetRevenueConfigById.RevenueConfig(data.revenueConfigId().getId(),
                                                      data.taxCodeId(),
                                                      data.taxCodeDescription(),
                                                      data.category().name(),
                                                      data.responsibleMinistryCode(),
                                                      this.revenuePartyName(data.responsibleMinistryCode()),
                                                      data.thirdPartyProviderCode(),
                                                      this.revenuePartyName(data.thirdPartyProviderCode()),
                                                      data.golPercentage(),
                                                      data.ministryPercentage(),
                                                      data.thirdPartyPercentage(),
                                                      data.sendingDfspPercentage(),
                                                      data.status(),
                                                      data.startDate(),
                                                      data.createdAt() == null ? null : data.createdAt().getEpochSecond(),
                                                      data.createdBy() == null ? null : this.utility.getEmail(new UserId(data.createdBy().getId())),
                                                      data.updatedAt() == null ? null : data.updatedAt().getEpochSecond(),
                                                      data.updatedBy() == null ? null : this.utility.getEmail(new UserId(data.updatedBy().getId())));
    }

    private String revenuePartyName(String partyCode) {

        if (partyCode == null || partyCode.isBlank()) {
            return null;
        }

        return this.revenuePartyQuery.get(partyCode)
                                     .map(RevenuePartyData::partyName)
                                     .orElse(null);
    }
}
