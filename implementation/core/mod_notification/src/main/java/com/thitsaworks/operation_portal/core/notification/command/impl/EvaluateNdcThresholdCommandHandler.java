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
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvaluateNdcThresholdCommandHandler
    implements EvaluateNdcThresholdCommand {

    private static final Logger LOG =
        LoggerFactory.getLogger(EvaluateNdcThresholdCommandHandler.class);

    private static final String NDC_THRESHOLD_FUNCTION = "NDC Threshold Triggered";

    private static final DateTimeFormatter ALERT_EVENT_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm:ss a 'UTC'", Locale.ENGLISH);

    private final NdcThresholdStateRepository stateRepository;
    private final NdcAlertEventRepository alertEventRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        Objects.requireNonNull(input.participantName());
        Objects.requireNonNull(input.currency());
        Objects.requireNonNull(input.currentPosition());
        Objects.requireNonNull(input.ndcLimit());
        Objects.requireNonNull(input.currentNdcUsed());
        Objects.requireNonNull(input.thresholdPercent());

        if (input.thresholdPercent().compareTo(BigDecimal.ZERO) < 0
                || input.thresholdPercent().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("thresholdPercent must be between 0 and 100");
        }

        NdcThresholdState state =
            stateRepository.findByParticipantAndCurrencyForUpdate(
                               input.participantName(), input.currency())
                           .orElseGet(() -> new NdcThresholdState(
                               input.participantName(),
                               input.currency(),
                               input.actor()
                           ));

        NdcThresholdStateType previousState = state.getCurrentState();

        int thresholdComparison = input.currentNdcUsed().compareTo(input.thresholdPercent());

        LOG.info("NDC threshold decision input: participant={}, currency={}, currentPosition={}, "
                     + "ndcLimit={}, ndcUsedPercent={}, thresholdPercent={}, previousState={}, comparison={}",
                 input.participantName(), input.currency(), input.currentPosition(), input.ndcLimit(),
                 input.currentNdcUsed(), input.thresholdPercent(), previousState,
                 thresholdComparison >= 0 ? "AT_OR_ABOVE_THRESHOLD" : "BELOW_THRESHOLD");

        state.recordEvaluation(
            input.currentPosition(),
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
                    input.participantName(),
                    input.currency(),
                    state.getBreachCycleNo(),
                    input.thresholdPercent(),
                    input.currentPosition(),
                    input.ndcLimit(),
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
            <html>
            <body style="font-family: Arial, sans-serif; color: #30343b;">
            <table style="border-collapse: collapse; width: 100%%; margin: 16px 0;">
                <thead>
                    <tr>
                        <th style="background-color: #f2f3f5; border: 1px solid #d9dde3; padding: 12px; text-align: left;">Date/Time</th>
                        <th style="background-color: #f2f3f5; border: 1px solid #d9dde3; padding: 12px; text-align: left;">Function</th>
                        <th style="background-color: #f2f3f5; border: 1px solid #d9dde3; padding: 12px; text-align: left;">Threshold Value</th>
                        <th style="background-color: #f2f3f5; border: 1px solid #d9dde3; padding: 12px; text-align: left;">Current Metric</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td style="border: 1px solid #d9dde3; padding: 12px;">%s</td>
                        <td style="border: 1px solid #d9dde3; padding: 12px;">%s</td>
                        <td style="border: 1px solid #d9dde3; padding: 12px;">%s</td>
                        <td style="border: 1px solid #d9dde3; padding: 12px;">%s</td>
                    </tr>
                </tbody>
            </table>
            </body>
            </html>
            """.formatted(
            buildSubject(input.participantName()),
            input.evaluatedAt().format(ALERT_EVENT_TIME_FORMATTER),
            NDC_THRESHOLD_FUNCTION,
            formatPercent(input.thresholdPercent()),
            formatPercent(input.currentNdcUsed())
        );
    }

    private String buildSubject(String participantName) {

        return "[ALERT][" + participantName + "] " + NDC_THRESHOLD_FUNCTION;
    }

    private String formatPercent(BigDecimal value) {

        return value.stripTrailingZeros().toPlainString() + "%";
    }

}
