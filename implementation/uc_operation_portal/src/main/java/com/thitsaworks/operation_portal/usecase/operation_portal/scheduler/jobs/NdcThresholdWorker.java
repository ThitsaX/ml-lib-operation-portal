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
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.hub_services.data.NdcUsedData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcUsedDataQuery;
import com.thitsaworks.operation_portal.core.notification.command.EvaluateNdcThresholdCommand;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdGateDecision;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdDetailRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("NdcThresholdWorker")
@ActionMetadata(category = ActionCategory.SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS)
public class NdcThresholdWorker
    extends ScheduledJob<SchedulerConfigData, List<NdcThresholdWorker.NdcEvaluation>> {

    private static final Logger LOG = LoggerFactory.getLogger(NdcThresholdWorker.class);

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    private final EvaluateNdcThresholdCommand evaluateNdcThresholdCommand;

    private final ParticipantNDCQuery participantNDCQuery;

    private final CreateJobExecutionLogCommand evaluationLogCreateCommand;

    private final ModifyJobExecutionLogCommand evaluationLogModifyCommand;

    private final GetNdcUsedDataQuery ndcUsedDataQuery;

    private final ThresholdDetailRepository thresholdDetailRepository;

    public NdcThresholdWorker(
        CreateJobExecutionLogCommand createJobExecutionLogCommand,
        ModifyJobExecutionLogCommand modifyJobExecutionLogCommand,
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ActionAuthorizationManager actionAuthorizationManager,
        ObjectMapper objectMapper,
        ThresholdConfigurationQuery thresholdConfigurationQuery,
        EvaluateNdcThresholdCommand evaluateNdcThresholdCommand,
        ParticipantNDCQuery participantNDCQuery,
        GetNdcUsedDataQuery getNdcUsedDataQuery,
        ThresholdDetailRepository thresholdDetailRepository) {
        super(
            createJobExecutionLogCommand,
            modifyJobExecutionLogCommand,
            createInputAuditCommand,
            createOutputAuditCommand,
            createExceptionAuditCommand,
            actionAuthorizationManager,
            objectMapper
        );

        this.thresholdConfigurationQuery = thresholdConfigurationQuery;
        this.evaluateNdcThresholdCommand = evaluateNdcThresholdCommand;
        this.participantNDCQuery = participantNDCQuery;
        this.evaluationLogCreateCommand = createJobExecutionLogCommand;
        this.evaluationLogModifyCommand = modifyJobExecutionLogCommand;
        this.ndcUsedDataQuery = getNdcUsedDataQuery;
        this.thresholdDetailRepository = thresholdDetailRepository;
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

        List<ThresholdDetail> enabledDetails = thresholdDetailRepository
            .findAllByStatusTrueOrderByDfspIdAscCurrencyAsc()
            .stream()
            .filter(this::isDetailGateAllowed)
            .toList();

        if (enabledDetails.isEmpty()) {
            LOG.info("Skipping NDC evaluation because no active DFSP/currency threshold details are enabled");
            return List.of();
        }

        Map<String, List<ThresholdDetail>> detailsByDfsp = enabledDetails.stream()
            .collect(Collectors.groupingBy(
                ThresholdDetail::getDfspId,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        LOG.info("NDC threshold details resolved: dfspCount={}, currencyConfigCount={}",
                 detailsByDfsp.size(), enabledDetails.size());

        List<NdcEvaluation> evaluations = new ArrayList<>();
        int skippedEvaluations = 0;
        int ndcUsedRecords = 0;

        for (Map.Entry<String, List<ThresholdDetail>> entry : detailsByDfsp.entrySet()) {

            String dfspId = entry.getKey();
            GetNdcUsedDataQuery.Output output = ndcUsedDataQuery.execute(
                new GetNdcUsedDataQuery.Input(dfspId));

            if (output == null || output.getNdcUsedData() == null) {
                LOG.warn("Skipping NDC evaluation because no NDC used data exists for DFSP [{}]", dfspId);
                skippedEvaluations += entry.getValue().size();
                continue;
            }

            Map<String, NdcUsedData> ndcUsedByCurrency = output.getNdcUsedData()
                .stream()
                .filter(NdcUsedData::isActive)
                .collect(Collectors.toMap(
                    NdcUsedData::currency,
                    Function.identity(),
                    (first, ignored) -> first,
                    LinkedHashMap::new
                ));

            ndcUsedRecords += ndcUsedByCurrency.size();

            for (ThresholdDetail detail : entry.getValue()) {

                NdcUsedData ndcUsedData = ndcUsedByCurrency.get(detail.getCurrency());
                if (ndcUsedData == null || ndcUsedData.ndcUsed() == null) {
                    LOG.warn("Skipping NDC evaluation because NDC used percent is missing for [{} / {}]",
                             detail.getDfspId(), detail.getCurrency());
                    skippedEvaluations++;
                    continue;
                }

                // Retained temporarily until runtime tables reference threshold_detail_id.
                ParticipantNDC participantNDC = participantNDCQuery
                    .get(detail.getDfspId(), detail.getCurrency())
                    .orElse(null);

                if (participantNDC == null) {
                    LOG.warn("Skipping NDC evaluation because the legacy runtime identity is missing for [{} / {}]",
                             detail.getDfspId(), detail.getCurrency());
                    skippedEvaluations++;
                    continue;
                }

                LocalDateTime evaluatedAt = LocalDateTime.now(ZoneId.of(schedulerConfigData.zoneId()))
                                                          .withNano(0);
                BigDecimal ndcUsedPercent = ndcUsedData.ndcUsed();
                String comparison = ndcUsedPercent.compareTo(detail.getNdcConfig()) >= 0
                    ? "AT_OR_ABOVE_THRESHOLD"
                    : "BELOW_THRESHOLD";

                LOG.info("NDC calculation result: participant={}, currency={}, ndcUsedPercent={}, "
                             + "thresholdPercent={}, comparison={}, calculationSource=GetNdcUsedDataQuery",
                         detail.getDfspId(), detail.getCurrency(), ndcUsedPercent,
                         detail.getNdcConfig(), comparison);

                EvaluateNdcThresholdCommand.Output thresholdOutput = evaluateNdcThresholdCommand.execute(
                    new EvaluateNdcThresholdCommand.Input(
                        participantNDC.getParticipantNDCId(),
                        detail.getDfspId(),
                        detail.getCurrency(),
                        null,
                        ndcUsedPercent,
                        detail.getNdcConfig(),
                        evaluatedAt,
                        "system"
                    )
                );

                NdcEvaluation evaluation = new NdcEvaluation(
                    detail.getDfspId(),
                    detail.getCurrency(),
                    ndcUsedPercent,
                    detail.getNdcConfig(),
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
        }

        long alertsCreated = evaluations.stream().filter(NdcEvaluation::alertCreated).count();
        long recoveries = evaluations.stream().filter(NdcEvaluation::recovered).count();

        LOG.info("NDC worker completed: ndcUsedRecords={}, evaluated={}, skipped={}, "
                     + "alertsCreated={}, recovered={}",
                 ndcUsedRecords, evaluations.size(), skippedEvaluations,
                 alertsCreated, recoveries);

        return evaluations;
    }

    private boolean isDetailGateAllowed(ThresholdDetail detail) {

        ThresholdGateDecision gate = thresholdConfigurationQuery.checkGate(Long.valueOf(detail.getDfspId()));
        if (!gate.allowed()) {
            LOG.info("Skipping threshold detail [{} / {}]: {}",
                     detail.getDfspId(), detail.getCurrency(), gate.reason());
        }

        return gate.allowed();
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
