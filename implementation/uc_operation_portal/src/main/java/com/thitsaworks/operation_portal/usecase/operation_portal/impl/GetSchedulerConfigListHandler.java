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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSchedulerConfigList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handler for retrieving all scheduler configurations with optional filtering and sorting.
 */
@Service
@ActionMetadata(category = ActionCategory.SCHEDULER_AND_JOB_CONFIGURATION)
public class GetSchedulerConfigListHandler
    extends OperationPortalUseCase<GetSchedulerConfigList.Input, GetSchedulerConfigList.Output>
    implements GetSchedulerConfigList {

    private static final Logger LOG = LoggerFactory.getLogger(GetSchedulerConfigListHandler.class);

    private final SchedulerConfigQuery schedulerConfigQuery;

    /**
     * Constructs a new handler with required dependencies.
     */
    public GetSchedulerConfigListHandler(PrincipalCache principalCache,
                                         SchedulerConfigQuery schedulerConfigQuery,
                                         ActionAuthorizationManager actionAuthorizationManager) {

        super(principalCache, actionAuthorizationManager);

        this.schedulerConfigQuery = schedulerConfigQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        LOG.info("Fetching scheduler configurations with filtering and sorting");

        // Create sort object based on input
        Sort sort = Sort.by(input.sortDirection(), input.sortBy());

        // Fetch filtered and sorted results
        List<SchedulerConfigData> configs = schedulerConfigQuery.getSchedulerConfigs(
            input.active(), sort);

        return new Output(configs);
    }

}
