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
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_config.query.RevenueRoundingPolicyQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenueRoundingPolicy;
import com.thitsaworks.operation_portal.usecase.util.Utility;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_CONFIG)
public class GetRevenueRoundingPolicyHandler
    extends OperationPortalUseCase<GetRevenueRoundingPolicy.Input, GetRevenueRoundingPolicy.Output>
    implements GetRevenueRoundingPolicy {

    private final RevenueRoundingPolicyQuery revenueRoundingPolicyQuery;

    private final Utility utility;

    public GetRevenueRoundingPolicyHandler(PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           RevenueRoundingPolicyQuery revenueRoundingPolicyQuery,
                                           Utility utility) {

        super(principalCache, actionAuthorizationManager);
        this.revenueRoundingPolicyQuery = revenueRoundingPolicyQuery;
        this.utility = utility;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        GetRevenueRoundingPolicy.RevenueRoundingPolicy policy = this.revenueRoundingPolicyQuery
                                                                    .findLatest()
                                                                    .map(
                                                                        data -> new GetRevenueRoundingPolicy.RevenueRoundingPolicy(
                                                                            data.revenueRoundingPolicyId(),
                                                                            data.roundingMode(),
                                                                            data.remainderRecipient(),
                                                                            data.createdAt(),
                                                                            data.createdBy() ==
                                                                                null ? null :
                                                                                this.utility.getEmail(
                                                                                    new UserId(
                                                                                        data
                                                                                            .createdBy()
                                                                                            .getId()))))
                                                                    .orElse(null);

        return new Output(policy);
    }

}
