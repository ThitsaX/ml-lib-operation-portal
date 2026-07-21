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
import com.thitsaworks.operation_portal.core.hub_services.data.NdcUsedData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcLedgerDataQuery;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcUsedDataQuery;
import com.thitsaworks.operation_portal.core.notification.command.EvaluateNdcThresholdCommand;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdGateDecision;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDC;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantNDCQuery;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.command.ModifyJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.ScheduledJob;
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

    private final GetNdcUsedDataQuery ndcUsedDataQuery;

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
        ParticipantNDCQuery participantNDCQuery,
        GetNdcUsedDataQuery getNdcUsedDataQuery) {
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
        this.ndcUsedDataQuery = getNdcUsedDataQuery;
    }

    @Override
    protected List<NdcEvaluation> onExecute(SchedulerConfigData schedulerConfigData)
        throws DomainException {

        var schemeConfiguration = thresholdConfigurationQuery.getSchemeConfiguration();

        if (schemeConfiguration.isEmpty() || !schemeConfiguration.get().thresholdEnabled()) {
            LOG.info("Skipping NDC evaluation because the scheme gate is OFF or unavailable");
            return List.of();
        }

        LOG.info("Starting NDC threshold evaluation because the scheme gate is ON");

        Set<Long> enabledParticipantCurrencyIds = thresholdConfigurationQuery.getAll()
                                                                .stream()
                                                                .filter(config -> config.scopeType() == ThresholdScopeType.DFSP)
                                                                .filter(config -> config.status() == NdcConfigurationStatus.ACTIVE)
                                                                .map(config -> config.participantCurrencyId())
                                                                .filter(participantCurrencyId -> participantCurrencyId != null)
                                                                .filter(this::isGateAllowed)
                                                                .collect(Collectors.toCollection(HashSet::new));

        if (enabledParticipantCurrencyIds.isEmpty()) {
            LOG.info("Skipping NDC evaluation because no DFSP threshold gates are ON");
            return List.of();
        }

        LOG.info("NDC enabled participant currencies resolved: count={}, participantCurrencyIds={}",
                 enabledParticipantCurrencyIds.size(), enabledParticipantCurrencyIds);

        var result = ledgerDataQuery.execute(
            new GetNdcLedgerDataQuery.Input(List.copyOf(enabledParticipantCurrencyIds))
        );

        LOG.info("Central Ledger NDC data fetched: requestedParticipantCurrencyCount={}, recordCount={}",
                 enabledParticipantCurrencyIds.size(), result.data().size());

        List<NdcEvaluation> evaluations = new ArrayList<>();
        int skippedEvaluations = 0;

        for (NdcLedgerData data : result.data()) {

            LOG.info("NDC ledger input: participant={}, currency={}, currentBalance={}, "
                         + "ndcLimitAmount={}, active={}",
                     data.participantName(), data.currency(), data.currentBalance(),
                     data.ndcLimitAmount(), data.active());

            if (!enabledParticipantCurrencyIds.contains(data.participantCurrencyId())) {
                LOG.warn("Ignoring ledger participantCurrencyId [{}] because it is not an enabled configured gate",
                         data.participantCurrencyId());
                skippedEvaluations++;
                continue;
            }

            if (!data.active()) {
                LOG.warn("Skipping inactive Central Ledger account for [{} / {}]",
                         data.participantName(), data.currency());
                skippedEvaluations++;
                continue;
            }

            ThresholdGateDecision gate = thresholdConfigurationQuery.checkGate(data.participantCurrencyId());
            if (!gate.allowed()) {
                LOG.info("Skipping NDC evaluation for DFSP [{}]: {}",
                         data.participantName(), gate.reason());
                skippedEvaluations++;
                continue;
            }

            ParticipantNDC participantNDC = participantNDCQuery
                .get(data.participantName(), data.currency())
                .orElse(null);

            if (participantNDC == null || participantNDC.getNdcPercent() == null) {
                LOG.warn("Skipping NDC evaluation because no NDC configuration exists for [{} / {}]",
                         data.participantName(), data.currency());
                skippedEvaluations++;
                continue;
            }

            if (data.ndcLimitAmount() == null || data.ndcLimitAmount().signum() <= 0) {
                LOG.warn("Skipping NDC evaluation because ledger NDC limit is missing or invalid for [{} / {}]",
                         data.participantName(), data.currency());
                skippedEvaluations++;
                continue;
            }

            LocalDateTime evaluatedAt = LocalDateTime.now(ZoneId.of(schedulerConfigData.zoneId()))
                                                      .withNano(0);
            var output = this.ndcUsedDataQuery.execute(new GetNdcUsedDataQuery.Input(data.participantName()));
            BigDecimal ndcUsedPercent = this.extractNdcUsedPercent(output, data);

            if (ndcUsedPercent == null) {
                LOG.warn("Skipping NDC evaluation because NDC used percent is missing for [{} / {}]",
                         data.participantName(), data.currency());
                skippedEvaluations++;
                continue;
            }

            String comparison = ndcUsedPercent.compareTo(participantNDC.getNdcPercent()) >= 0
                ? "AT_OR_ABOVE_THRESHOLD"
                : "BELOW_THRESHOLD";

            LOG.info("NDC calculation result: participant={}, currency={}, ndcUsedPercent={}, "
                         + "thresholdPercent={}, comparison={}, calculationSource=GetNdcUsedDataQuery",
                     data.participantName(), data.currency(), ndcUsedPercent,
                     participantNDC.getNdcPercent(), comparison);

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

            String decision;
            if (thresholdOutput.alertCreated()) {
                decision = "CREATE_ALERT";
            } else if (thresholdOutput.recovered()) {
                decision = "RECOVERED";
            } else if (thresholdOutput.currentState() == NdcThresholdStateType.BREACHED) {
                decision = "SUPPRESS_DUPLICATE";
            } else {
                decision = "NO_ALERT";
            }

            LOG.info("NDC evaluation decision: participant={}, currency={}, previousState={}, "
                         + "currentState={}, breachCycle={}, decision={}, alertEventId={}",
                     evaluation.participantName(), evaluation.currency(), evaluation.previousState(),
                     evaluation.currentState(), evaluation.breachCycleNo(), decision,
                     evaluation.alertEventId());

            persistEvaluationLog(schedulerConfigData, evaluation, evaluatedAt);
            evaluations.add(evaluation);
        }

        long alertsCreated = evaluations.stream().filter(NdcEvaluation::alertCreated).count();
        long recoveries = evaluations.stream().filter(NdcEvaluation::recovered).count();

        LOG.info("NDC worker completed: ledgerRecords={}, evaluated={}, skipped={}, "
                     + "alertsCreated={}, recovered={}",
                 result.data().size(), evaluations.size(), skippedEvaluations,
                 alertsCreated, recoveries);

        return evaluations;
    }

    private boolean isGateAllowed(Long participantCurrencyId) {

        return thresholdConfigurationQuery.checkGate(participantCurrencyId).allowed();
    }

    private BigDecimal extractNdcUsedPercent(GetNdcUsedDataQuery.Output output, NdcLedgerData data) {

        if (output == null || output.getNdcUsedData() == null) {
            return null;
        }

        return output.getNdcUsedData()
                     .stream()
                     .filter(NdcUsedData::isActive)
                     .filter(ndcUsedData -> data.currency().equals(ndcUsedData.currency()))
                     .map(NdcUsedData::ndcUsed)
                     .filter(ndcUsed -> ndcUsed != null)
                     .findFirst()
                     .orElse(null);
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
