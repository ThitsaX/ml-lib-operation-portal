package com.thitsaworks.operation_portal.core.notification.command.impl;

import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.notification.command.EvaluateNdcThresholdCommand;
import com.thitsaworks.operation_portal.core.notification.model.NdcAlertEvent;
import com.thitsaworks.operation_portal.core.notification.model.NdcThresholdState;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcAlertEventRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcThresholdStateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvaluateNdcThresholdCommandHandler
    implements EvaluateNdcThresholdCommand {

    private static final Logger LOG =
        LoggerFactory.getLogger(EvaluateNdcThresholdCommandHandler.class);

    private static final String NDC_USAGE_ALERT_SUBJECT = "NDC Usage Alert – Action Required";

    private final NdcThresholdStateRepository stateRepository;
    private final NdcAlertEventRepository alertEventRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        Objects.requireNonNull(input.participantNDCId());
        Objects.requireNonNull(input.currentNdcUsed());
        Objects.requireNonNull(input.thresholdPercent());

        if (input.thresholdPercent().compareTo(BigDecimal.ZERO) < 0
                || input.thresholdPercent().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("thresholdPercent must be between 0 and 100");
        }

        NdcThresholdState state =
            stateRepository.findByParticipantNDCIdForUpdate(input.participantNDCId())
                           .orElseGet(() -> new NdcThresholdState(
                               input.participantNDCId(),
                               input.actor()
                           ));

        NdcThresholdStateType previousState = state.getCurrentState();

        int thresholdComparison = input.currentNdcUsed().compareTo(input.thresholdPercent());

        LOG.info("NDC threshold decision input: participant={}, currency={}, currentBalance={}, "
                     + "ndcUsedPercent={}, thresholdPercent={}, previousState={}, comparison={}",
                 input.participantName(), input.currency(), input.currentBalance(),
                 input.currentNdcUsed(), input.thresholdPercent(), previousState,
                 thresholdComparison >= 0 ? "AT_OR_ABOVE_THRESHOLD" : "BELOW_THRESHOLD");

        state.recordEvaluation(
            input.currentBalance(),
            input.currentNdcUsed(),
            input.actor()
                              );

        boolean alertCreated = false;
        boolean recovered = false;
        NdcAlertEvent alertEvent = null;

        if (thresholdComparison >= 0) {
            alertCreated = state.breach(input.evaluatedAt(), input.actor());

            if (alertCreated) {
                alertEvent = new NdcAlertEvent(
                    input.participantNDCId(),
                    input.participantName(),
                    input.currency(),
                    state.getBreachCycleNo(),
                    input.thresholdPercent(),
                    input.currentBalance(),
                    input.currentNdcUsed(),
                    buildNdcUsageAlertEventMessage(input),
                    input.evaluatedAt(),
                    input.actor()
                );
            }
        } else {
            recovered = state.recover(input.evaluatedAt(), input.actor());
        }

        stateRepository.saveAndFlush(state);

        if (alertEvent != null) {
            alertEventRepository.saveAndFlush(alertEvent);
        }

        String decision;
        if (alertCreated) {
            decision = "CREATE_ALERT";
        } else if (recovered) {
            decision = "RECOVERED";
        } else if (state.getCurrentState() == NdcThresholdStateType.BREACHED) {
            decision = "SUPPRESS_DUPLICATE";
        } else {
            decision = "NO_ALERT";
        }

        LOG.info("NDC threshold decision result: participant={}, currency={}, previousState={}, "
                     + "currentState={}, breachCycle={}, decision={}, alertEventId={}",
                 input.participantName(), input.currency(), previousState, state.getCurrentState(),
                 state.getBreachCycleNo(), decision,
                 alertEvent == null ? null : alertEvent.getNdcAlertEventId());

        return new Output(
            previousState,
            state.getCurrentState(),
            state.getBreachCycleNo(),
            alertCreated,
            recovered,
            alertEvent == null ? null : alertEvent.getNdcAlertEventId()
        );
    }

    private String buildNdcUsageAlertEventMessage(Input input) {

        return """
            %s,
            Dear User,
            Your %s account has reached %s%% of its NDC usage limit.
            Please deposit additional funds to prevent transaction blockage.
            DFSP: %s
            Currency: %s
            Current NDC Usage: %s%%
            This is an automated notification. Please do not reply to this email.
            Regards,
            Operations Team
            """.formatted(
            NDC_USAGE_ALERT_SUBJECT,
            input.currency(),
            input.currentNdcUsed().toPlainString(),
            input.participantName(),
            input.currency(),
            input.currentNdcUsed().toPlainString()
        );
    }
}
