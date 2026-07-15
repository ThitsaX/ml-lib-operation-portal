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
import com.thitsaworks.operation_portal.core.hub_services.data.NdcLedgerData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcLedgerDataQuery;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.ModifyJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ScheduledJob;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ndc.NdcUsedPercentCalculator;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Component;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;

@Component("NdcThresholdWorker")
@ActionMetadata(category = ActionCategory.SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS)
public class NdcThresholdWorker
    extends ScheduledJob<SchedulerConfigData, List<NdcThresholdWorker.NdcEvaluation>> {

    private static final Logger LOG =
        LoggerFactory.getLogger(NdcThresholdWorker.class);

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    private final GetNdcLedgerDataQuery ledgerDataQuery;

    public NdcThresholdWorker(
        CreateJobExecutionLogCommand createJobExecutionLogCommand,
        ModifyJobExecutionLogCommand modifyJobExecutionLogCommand,
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ActionAuthorizationManager actionAuthorizationManager,
        ObjectMapper objectMapper,
        GetNdcLedgerDataQuery ledgerDataQuery,
        ThresholdConfigurationQuery thresholdConfigurationQuery
                             ) {
        super(
            createJobExecutionLogCommand,
            modifyJobExecutionLogCommand,
            createInputAuditCommand,
            createOutputAuditCommand,
            createExceptionAuditCommand,
            actionAuthorizationManager,
            objectMapper
             );

        this.ledgerDataQuery = ledgerDataQuery;
        this.thresholdConfigurationQuery = thresholdConfigurationQuery;
    }

    @Override
    protected List<NdcEvaluation> onExecute(
        SchedulerConfigData schedulerConfigData
                                           ) throws DomainException {

        var schemeConfiguration = thresholdConfigurationQuery.getSchemeConfiguration();

        if (schemeConfiguration.isEmpty() || !schemeConfiguration.get().thresholdEnabled()) {
            LOG.info("Skipping NDC evaluation because the scheme gate is OFF or unavailable");
            return List.of();
        }

        List<String> enabledDfspIds = thresholdConfigurationQuery.getAll()
                                                                  .stream()
                                                                  .filter(config -> config.scopeType() == ThresholdScopeType.DFSP)
                                                                  .filter(config -> config.status() == NdcConfigurationStatus.ACTIVE)
                                                                  .filter(config -> config.thresholdEnabled())
                                                                  .map(config -> config.dfspId())
                                                                  .filter(dfspId -> dfspId != null && !dfspId.isBlank())
                                                                  .distinct()
                                                                  .toList();

        if (enabledDfspIds.isEmpty()) {
            LOG.info("Skipping NDC evaluation because no DFSP threshold gates are ON");
            return List.of();
        }

        var result = ledgerDataQuery.execute(
            new GetNdcLedgerDataQuery.Input(enabledDfspIds)
                                            );

        return result.data()
                     .stream()
                     .filter(NdcLedgerData::active)
                     .map(this::evaluate)
                     .toList();
    }

    private NdcEvaluation evaluate(NdcLedgerData data) {

        BigDecimal ndcUsedPercent =
            NdcUsedPercentCalculator.calculate(
                data.currentBalance(),
                data.ndcLimitAmount()
                                              );

        return new NdcEvaluation(
            data.participantName(),
            data.currency(),
            data.currentBalance(),
            data.ndcLimitAmount(),
            ndcUsedPercent
        );
    }

    public record NdcEvaluation(
        String participantName,
        String currency,
        BigDecimal currentBalance,
        BigDecimal ndcLimitAmount,
        BigDecimal ndcUsedPercent
    ) {
    }

}
