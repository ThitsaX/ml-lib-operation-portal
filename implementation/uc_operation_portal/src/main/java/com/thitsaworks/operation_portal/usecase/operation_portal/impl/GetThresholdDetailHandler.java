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

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdDetailData;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdDetailQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetThresholdDetail;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class GetThresholdDetailHandler
    extends OperationPortalUseCase<GetThresholdDetail.Input, GetThresholdDetail.Output>
    implements GetThresholdDetail {

    private final ThresholdDetailQuery thresholdDetailQuery;

    public GetThresholdDetailHandler(PrincipalCache principalCache,
                                     ActionAuthorizationManager actionAuthorizationManager,
                                     ThresholdDetailQuery thresholdDetailQuery) {

        super(principalCache, actionAuthorizationManager);
        this.thresholdDetailQuery = thresholdDetailQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        ThresholdDetailData detail = this.thresholdDetailQuery.get(new ThresholdDetailId(input.id()))
                                                             .orElseThrow(() -> new InputException(
                                                                 new ErrorMessage(
                                                                     "THRESHOLD_DETAIL_NOT_FOUND",
                                                                     "Threshold detail was not found.")));

        return new Output(toOutput(detail));
    }

    static GetThresholdDetail.ThresholdDetail toOutput(ThresholdDetailData data) {

        return new GetThresholdDetail.ThresholdDetail(
            data.thresholdDetailId().getEntityId(),
            data.thresholdConfigurationId().getEntityId(),
            data.currency(),
            data.visualConfig(),
            data.ndcConfig(),
            data.status(),
            data.createdAt(),
            data.createdBy(),
            data.updatedAt(),
            data.updatedBy());
    }
}
