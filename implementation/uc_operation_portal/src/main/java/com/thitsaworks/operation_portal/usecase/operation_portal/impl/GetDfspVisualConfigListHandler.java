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
import com.thitsaworks.operation_portal.core.notification.data.DfspThresholdDetailData;
import com.thitsaworks.operation_portal.core.notification.query.DfspThresholdDetailQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetDfspVisualConfigList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class GetDfspVisualConfigListHandler
    extends OperationPortalUseCase<GetDfspVisualConfigList.Input, GetDfspVisualConfigList.Output>
    implements GetDfspVisualConfigList {

    private final DfspThresholdDetailQuery dfspThresholdDetailQuery;

    public GetDfspVisualConfigListHandler(PrincipalCache principalCache,
                                          ActionAuthorizationManager actionAuthorizationManager,
                                          DfspThresholdDetailQuery dfspThresholdDetailQuery) {
        super(principalCache, actionAuthorizationManager);
        this.dfspThresholdDetailQuery = dfspThresholdDetailQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        List<DfspThresholdDetailData> details = this.dfspThresholdDetailQuery.getAllDfspThresholdDetails();

        List<DfspThresholdDetailItem> items = details.stream()
            .map(detail -> new DfspThresholdDetailItem(
                detail.dfspId(),
                detail.currency(),
                detail.visualConfig().toString()
            ))
            .collect(Collectors.toList());

        return new Output(new DfspThresholdDetailList(items));
    }
}
