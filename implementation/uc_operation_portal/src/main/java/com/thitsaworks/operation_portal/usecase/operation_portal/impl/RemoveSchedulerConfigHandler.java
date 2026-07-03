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
import com.thitsaworks.operation_portal.core.scheduler.command.DeleteSchedulerConfigCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.RemoveSchedulerConfig;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.SchedulerEngine;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.SCHEDULER_AND_JOB_CONFIGURATION)
public class RemoveSchedulerConfigHandler
    extends OperationPortalUseCase<RemoveSchedulerConfig.Input, RemoveSchedulerConfig.Output>
    implements RemoveSchedulerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveSchedulerConfigHandler.class);

    private final DeleteSchedulerConfigCommand deleteSchedulerConfigCommand;

    private final SchedulerEngine schedulerEngine;

    public RemoveSchedulerConfigHandler(PrincipalCache principalCache,
                                        DeleteSchedulerConfigCommand deleteSchedulerConfigCommand,
                                        ActionAuthorizationManager actionAuthorizationManager,
                                        SchedulerEngine schedulerEngine) {

        super(principalCache, actionAuthorizationManager);

        this.deleteSchedulerConfigCommand = deleteSchedulerConfigCommand;
        this.schedulerEngine = schedulerEngine;
    }

    @Override
    protected RemoveSchedulerConfig.Output onExecute(Input input) throws DomainException {

        LOG.info("Deleting scheduler config with ID: {}", input.schedulerConfigId());

        var output = this.deleteSchedulerConfigCommand.execute(input.schedulerConfigId());

        this.schedulerEngine.cancel(input.schedulerConfigId().getId());

        return new Output(output.deleted());
    }

}