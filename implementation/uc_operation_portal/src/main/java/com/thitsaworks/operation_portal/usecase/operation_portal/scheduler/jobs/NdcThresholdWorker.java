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
import com.thitsaworks.operation_portal.core.email.EmailService;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.ModifyJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ScheduledJob;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("NdcThresholdWorker")
@ActionMetadata(category = ActionCategory.SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS)
public class NdcThresholdWorker
    extends ScheduledJob<SchedulerConfigData, NdcThresholdWorker.Output> {

    private static final Logger LOG = LoggerFactory.getLogger(NdcThresholdWorker.class);


    public NdcThresholdWorker(CreateJobExecutionLogCommand createJobExecutionLogCommand,
                              ModifyJobExecutionLogCommand modifyJobExecutionLogCommand,
                              CreateInputAuditCommand createInputAuditCommand,
                              CreateOutputAuditCommand createOutputAuditCommand,
                              CreateExceptionAuditCommand createExceptionAuditCommand,
                              ActionAuthorizationManager actionAuthorizationManager,
                              ObjectMapper objectMapper) {

        super(
            createJobExecutionLogCommand, modifyJobExecutionLogCommand, createInputAuditCommand,
            createOutputAuditCommand, createExceptionAuditCommand, actionAuthorizationManager,
            objectMapper);
    }

    @Override
    protected Output onExecute(SchedulerConfigData schedulerConfigData) throws DomainException, InterruptedException {

        LOG.info("NDC Threshold Worker execution started for config: [{}]", schedulerConfigData.name());

        // TODO: Implement NDC threshold evaluation logic here
        // This should:
        // 1. Query participant NDC balances
        // 2. Evaluate against threshold configurations
        // 3. Update NdcThresholdState records
        // 4. Generate alerts if thresholds are breached
        //
        // Example usage when threshold is breached:
        // if (emailService != null) {
        //     try {
        //         emailService.sendNdcUsageAlertToUser(
        //             userId,           // The user to notify
        //             dfspName,         // DFSP name
        //             currency,         // Currency code
        //             ndcUsedPercentage // Current NDC usage percentage (e.g., new BigDecimal("80.5"))
        //         );
        //     } catch (ParticipantException e) {
        //         LOG.error("Failed to send NDC alert email to user: {}", userId, e);
        //     }
        // } else {
        //     LOG.warn("EmailService not available, skipping email alert");
        // }

        LOG.info("NDC Threshold Worker execution completed for config: [{}]", schedulerConfigData.name());

        return new Output(true);
    }

    public record Output(boolean success) {

    }

}
