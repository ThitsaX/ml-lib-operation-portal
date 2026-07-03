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
package com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.ModifyJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("SchedulingTester")
@ActionMetadata(category = ActionCategory.SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS)
public class SchedulingTester {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulingTester.class);

    public SchedulingTester(CreateJobExecutionLogCommand createJobExecutionLogCommand,
                            ModifyJobExecutionLogCommand modifyJobExecutionLogCommand,
                            CreateInputAuditCommand createInputAuditCommand,
                            CreateOutputAuditCommand createOutputAuditCommand,
                            CreateExceptionAuditCommand createExceptionAuditCommand,
                            ActionAuthorizationManager actionAuthorizationManager,
                            ObjectMapper objectMapper) {

    }

    protected SchedulingTester.Output onExecute(SchedulerConfigData schedulerConfigData)
        throws DomainException, InterruptedException {

        LOG.info("Running SchedulingTester job: [{}]", schedulerConfigData);

        Thread.sleep(10000);

        LOG.info("SchedulingTester job:[{}] completed", schedulerConfigData);

        return new Output(
            String.format("SchedulingTester job:[%s] completed", schedulerConfigData));

    }

    public record Output(String outputLog) { }

}
