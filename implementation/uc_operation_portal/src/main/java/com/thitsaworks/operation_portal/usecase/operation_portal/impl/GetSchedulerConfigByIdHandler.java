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
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.core.scheduler.query.SchedulerConfigQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSchedulerConfigById;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.SCHEDULER_AND_JOB_CONFIGURATION)
public class GetSchedulerConfigByIdHandler
    extends OperationPortalUseCase<GetSchedulerConfigById.Input, GetSchedulerConfigById.Output>
    implements GetSchedulerConfigById {

    private static final Logger LOG = LoggerFactory.getLogger(GetSchedulerConfigByIdHandler.class);

    private final SchedulerConfigQuery schedulerConfigQuery;

    public GetSchedulerConfigByIdHandler(PrincipalCache principalCache,
                                         SchedulerConfigQuery schedulerConfigQuery,
                                         ActionAuthorizationManager actionAuthorizationManager) {

        super(principalCache, actionAuthorizationManager);
        this.schedulerConfigQuery = schedulerConfigQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        LOG.info("Fetching scheduler configuration with ID: {}", input.schedulerConfigId());
        SchedulerConfigData config = schedulerConfigQuery.get(input.schedulerConfigId());
        return new Output(config);
    }

}
