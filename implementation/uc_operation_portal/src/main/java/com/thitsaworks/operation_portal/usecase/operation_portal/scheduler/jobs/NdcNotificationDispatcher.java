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
import com.thitsaworks.operation_portal.component.common.identifier.NdcNotificationDispatchLogId;
import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.notification.model.NdcAlertEvent;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcAlertEventRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcNotificationDispatchLogRepository;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.ModifyJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.operation_portal.notification.NdcNotificationDispatchService;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ScheduledJob;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component("NdcNotificationDispatcher")
@ActionMetadata(category = ActionCategory.SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS)
public class NdcNotificationDispatcher
    extends ScheduledJob<SchedulerConfigData, NdcNotificationDispatcher.DispatchSummary> {

    private static final Logger LOG = LoggerFactory.getLogger(NdcNotificationDispatcher.class);

    private static final int EVENT_BATCH_SIZE = 100; // old alert log limit

    private static final int MAXIMUM_ATTEMPTS = 3;

    private static final Duration RETRY_DELAY = Duration.ofMinutes(5);

    private final NdcAlertEventRepository alertEventRepository;

    private final NdcNotificationDispatchLogRepository dispatchLogRepository;

    private final NdcNotificationDispatchService dispatchService;

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    public NdcNotificationDispatcher(
        CreateJobExecutionLogCommand createJobExecutionLogCommand,
        ModifyJobExecutionLogCommand modifyJobExecutionLogCommand,
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ActionAuthorizationManager actionAuthorizationManager,
        ObjectMapper objectMapper,
        NdcAlertEventRepository alertEventRepository,
        NdcNotificationDispatchLogRepository dispatchLogRepository,
        NdcNotificationDispatchService dispatchService,
        ThresholdConfigurationQuery thresholdConfigurationQuery) {

        super(
            createJobExecutionLogCommand,
            modifyJobExecutionLogCommand,
            createInputAuditCommand,
            createOutputAuditCommand,
            createExceptionAuditCommand,
            actionAuthorizationManager,
            objectMapper);

        this.alertEventRepository = alertEventRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.dispatchService = dispatchService;
        this.thresholdConfigurationQuery = thresholdConfigurationQuery;
    }

    @Override
    protected DispatchSummary onExecute(SchedulerConfigData schedulerConfigData)
        throws DomainException {

        boolean schemeEnabled =
            thresholdConfigurationQuery.getSchemeConfiguration()
                                       .map(configuration -> configuration.thresholdEnabled())
                                       .orElse(false);

        if (!schemeEnabled) {
            LOG.debug("Skipping NDC notification dispatch because the scheme gate is OFF or unavailable");
            return DispatchSummary.empty();
        }

        int prepared = 0;
        int sent = 0;
        int failed = 0;
        int skipped = 0;

        List<NdcAlertEvent> undispatchedEvents =
            alertEventRepository.findUndispatched(PageRequest.of(0, EVENT_BATCH_SIZE));

        if (!undispatchedEvents.isEmpty()) {
            LOG.info("NDC notification dispatcher found new alert events: count={}",
                     undispatchedEvents.size());
        }

        for (NdcAlertEvent alertEvent : undispatchedEvents) {

            List<NdcNotificationDispatchLogId> dispatchLogIds =
                dispatchService.createDispatchLogs(alertEvent);

            prepared += dispatchLogIds.size();

            for (NdcNotificationDispatchLogId dispatchLogId : dispatchLogIds) {

                var result = dispatchService.deliver(dispatchLogId);

                if (result.sent()) {
                    sent++;
                } else if (result.failed()) {
                    failed++;
                } else {
                    skipped++;
                }
            }
        }

        LocalDateTime retryBefore = LocalDateTime.now().minus(RETRY_DELAY);

        var retryableLogs = dispatchLogRepository.findRetryable(
            List.of(
                NdcDeliveryStatus.PENDING,
                NdcDeliveryStatus.FAILED,
                NdcDeliveryStatus.RETRYING),
            MAXIMUM_ATTEMPTS,
            toInstant(retryBefore),
            PageRequest.of(0, EVENT_BATCH_SIZE));

        if (!retryableLogs.isEmpty()) {
            LOG.info("NDC notification dispatcher found retryable deliveries: count={}, retryBefore={}",
                     retryableLogs.size(), retryBefore);
        }

        for (var dispatchLog : retryableLogs) {

            var result = dispatchService.deliver(
                dispatchLog.getNdcNotificationDispatchLogId());

            if (result.sent()) {
                sent++;
            } else if (result.failed()) {
                failed++;
            } else {
                skipped++;
            }
        }

        DispatchSummary summary = new DispatchSummary(
            undispatchedEvents.size(),
            retryableLogs.size(),
            prepared,
            sent,
            failed,
            skipped);

        if (summary.hasWork()) {
            LOG.info("NDC notification dispatcher completed: events={}, retries={}, prepared={}, "
                         + "sent={}, failed={}, skipped={}",
                     summary.undispatchedEvents(), summary.retryableDeliveries(),
                     summary.preparedRecipients(), summary.sent(), summary.failed(), summary.skipped());
        }

        return summary;
    }

    @Override
    protected boolean deferExecutionLog() {

        return true;
    }

    @Override
    protected boolean shouldPersistExecutionLog(DispatchSummary summary) {

        return summary.hasDeliveryOutcome();
    }

    @Override
    protected String buildExecutionMessage(SchedulerConfigData schedulerConfigData,
                                           DispatchSummary summary,
                                           LocalDateTime endTime) {

        return String.format(
            "NDC notification dispatcher completed: events=%d, retries=%d, prepared=%d, sent=%d, failed=%d, skipped=%d at [%s (%s)]",
            summary.undispatchedEvents(),
            summary.retryableDeliveries(),
            summary.preparedRecipients(),
            summary.sent(),
            summary.failed(),
            summary.skipped(),
            endTime,
            schedulerConfigData.zoneId()
        );
    }

    private Instant toInstant(LocalDateTime value) {

        return value.toInstant(ZoneOffset.UTC);
    }

    public record DispatchSummary(int undispatchedEvents,
                                  int retryableDeliveries,
                                  int preparedRecipients,
                                  int sent,
                                  int failed,
                                  int skipped) {

        public boolean hasWork() {

            return this.undispatchedEvents > 0 || this.retryableDeliveries > 0;
        }

        public boolean hasDeliveryOutcome() {

            return this.sent > 0 || this.failed > 0;
        }

        public static DispatchSummary empty() {

            return new DispatchSummary(0, 0, 0, 0, 0, 0);
        }
    }
}
