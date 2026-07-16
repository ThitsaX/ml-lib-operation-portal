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
import com.thitsaworks.operation_portal.component.common.type.JobStatus;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.hub_services.data.NdcLedgerData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcLedgerDataQuery;
import com.thitsaworks.operation_portal.core.notification.command.EvaluateNdcThresholdCommand;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdGateDecision;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDC;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantNDCQuery;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.ModifyJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ScheduledJob;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ndc.NdcUsedPercentCalculator;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("NdcThresholdWorker")
@ActionMetadata(category = ActionCategory.SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS)
public class NdcThresholdWorker
    extends ScheduledJob<SchedulerConfigData, List<NdcThresholdWorker.NdcEvaluation>> {

    private static final Logger LOG = LoggerFactory.getLogger(NdcThresholdWorker.class);

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    private final GetNdcLedgerDataQuery ledgerDataQuery;

    private final EvaluateNdcThresholdCommand evaluateNdcThresholdCommand;

    private final ParticipantNDCQuery participantNDCQuery;

    private final CreateJobExecutionLogCommand evaluationLogCreateCommand;

    private final ModifyJobExecutionLogCommand evaluationLogModifyCommand;

    public NdcThresholdWorker(
        CreateJobExecutionLogCommand createJobExecutionLogCommand,
        ModifyJobExecutionLogCommand modifyJobExecutionLogCommand,
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ActionAuthorizationManager actionAuthorizationManager,
        ObjectMapper objectMapper,
        GetNdcLedgerDataQuery ledgerDataQuery,
        ThresholdConfigurationQuery thresholdConfigurationQuery,
        EvaluateNdcThresholdCommand evaluateNdcThresholdCommand,
        ParticipantNDCQuery participantNDCQuery
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
        this.evaluateNdcThresholdCommand = evaluateNdcThresholdCommand;
        this.participantNDCQuery = participantNDCQuery;
        this.evaluationLogCreateCommand = createJobExecutionLogCommand;
        this.evaluationLogModifyCommand = modifyJobExecutionLogCommand;
    }

    @Override
    protected List<NdcEvaluation> onExecute(SchedulerConfigData schedulerConfigData)
        throws DomainException {

        var schemeConfiguration = thresholdConfigurationQuery.getSchemeConfiguration();

        if (schemeConfiguration.isEmpty() || !schemeConfiguration.get().thresholdEnabled()) {
            LOG.info("Skipping NDC evaluation because the scheme gate is OFF or unavailable");
            return List.of();
        }

        Set<String> enabledDfspIds = thresholdConfigurationQuery.getAll()
                                                                .stream()
                                                                .filter(config -> config.scopeType() == ThresholdScopeType.DFSP)
                                                                .filter(config -> config.status() == NdcConfigurationStatus.ACTIVE)
                                                                .map(config -> config.dfspId())
                                                                .filter(dfspId -> dfspId != null && !dfspId.isBlank())
                                                                .filter(this::isGateAllowed)
                                                                .collect(Collectors.toCollection(HashSet::new));

        if (enabledDfspIds.isEmpty()) {
            LOG.info("Skipping NDC evaluation because no DFSP threshold gates are ON");
            return List.of();
        }

        var result = ledgerDataQuery.execute(
            new GetNdcLedgerDataQuery.Input(List.copyOf(enabledDfspIds))
        );

        List<NdcEvaluation> evaluations = new ArrayList<>();

        for (NdcLedgerData data : result.data()) {

            if (!enabledDfspIds.contains(data.participantName())) {
                LOG.warn("Ignoring ledger participant [{}] because it is not an enabled configured DFSP",
                         data.participantName());
                continue;
            }

            if (!data.active()) {
                continue;
            }

            ThresholdGateDecision gate = thresholdConfigurationQuery.checkGate(data.participantName());
            if (!gate.allowed()) {
                LOG.info("Skipping NDC evaluation for DFSP [{}]: {}",
                         data.participantName(), gate.reason());
                continue;
            }

            ParticipantNDC participantNDC = participantNDCQuery
                .get(data.participantName(), data.currency())
                .orElse(null);

            if (participantNDC == null || participantNDC.getNdcPercent() == null) {
                LOG.warn("Skipping NDC evaluation because no NDC configuration exists for [{} / {}]",
                         data.participantName(), data.currency());
                continue;
            }

            if (data.ndcLimitAmount() == null || data.ndcLimitAmount().signum() <= 0) {
                LOG.warn("Skipping NDC evaluation because ledger NDC limit is missing or invalid for [{} / {}]",
                         data.participantName(), data.currency());
                continue;
            }

            BigDecimal ndcUsedPercent = NdcUsedPercentCalculator.calculate(
                data.currentBalance(),
                data.ndcLimitAmount()
            );

            LocalDateTime evaluatedAt = LocalDateTime.now(ZoneId.of(schedulerConfigData.zoneId()))
                                                      .withNano(0);

            EvaluateNdcThresholdCommand.Output thresholdOutput = evaluateNdcThresholdCommand.execute(
                new EvaluateNdcThresholdCommand.Input(
                    participantNDC.getParticipantNDCId(),
                    data.participantName(),
                    data.currency(),
                    data.currentBalance(),
                    ndcUsedPercent,
                    participantNDC.getNdcPercent(),
                    evaluatedAt,
                    "system"
                )
            );

            NdcEvaluation evaluation = new NdcEvaluation(
                data.participantName(),
                data.currency(),
                data.currentBalance(),
                data.ndcLimitAmount(),
                ndcUsedPercent,
                participantNDC.getNdcPercent(),
                thresholdOutput.previousState(),
                thresholdOutput.currentState(),
                thresholdOutput.breachCycleNo(),
                thresholdOutput.alertCreated(),
                thresholdOutput.recovered(),
                thresholdOutput.alertEventId()
            );

            persistEvaluationLog(schedulerConfigData, evaluation, evaluatedAt);
            evaluations.add(evaluation);
        }

        return evaluations;
    }

    private boolean isGateAllowed(String dfspId) {

        return thresholdConfigurationQuery.checkGate(dfspId).allowed();
    }

    private void persistEvaluationLog(SchedulerConfigData schedulerConfigData,
                                      NdcEvaluation evaluation,
                                      LocalDateTime evaluatedAt) throws DomainException {

        var started = evaluationLogCreateCommand.execute(
            new CreateJobExecutionLogCommand.Input(
                schedulerConfigData.name(),
                JobStatus.STARTED,
                evaluatedAt,
                evaluation.participantName(),
                evaluation.currency(),
                evaluation.ndcUsedPercent(),
                evaluation.thresholdPercent()
            )
        );

        String message = String.format(
            "NDC evaluated: used=%s%%, threshold=%s%%, state=%s -> %s, alertCreated=%s, recovered=%s",
            evaluation.ndcUsedPercent(),
            evaluation.thresholdPercent(),
            evaluation.previousState(),
            evaluation.currentState(),
            evaluation.alertCreated(),
            evaluation.recovered()
        );

        evaluationLogModifyCommand.execute(
            new ModifyJobExecutionLogCommand.Input(
                started.jobExecutionLogId(),
                JobStatus.COMPLETED,
                message,
                evaluatedAt,
                evaluation.participantName(),
                evaluation.currency(),
                evaluation.ndcUsedPercent(),
                evaluation.thresholdPercent()
            )
        );
    }

    public record NdcEvaluation(
        String participantName,
        String currency,
        BigDecimal currentBalance,
        BigDecimal ndcLimitAmount,
        BigDecimal ndcUsedPercent,
        BigDecimal thresholdPercent,
        NdcThresholdStateType previousState,
        NdcThresholdStateType currentState,
        long breachCycleNo,
        boolean alertCreated,
        boolean recovered,
        com.thitsaworks.operation_portal.component.common.identifier.NdcAlertEventId alertEventId
    ) {
    }
}
