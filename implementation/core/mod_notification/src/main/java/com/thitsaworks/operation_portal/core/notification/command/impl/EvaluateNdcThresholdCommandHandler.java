package com.thitsaworks.operation_portal.core.notification.command.impl;

import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.notification.command.EvaluateNdcThresholdCommand;
import com.thitsaworks.operation_portal.core.notification.model.NdcAlertEvent;
import com.thitsaworks.operation_portal.core.notification.model.NdcThresholdState;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcAlertEventRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcThresholdStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvaluateNdcThresholdCommandHandler
    implements EvaluateNdcThresholdCommand {

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

        state.recordEvaluation(
            input.currentBalance(),
            input.currentNdcUsed(),
            input.actor()
                              );

        boolean alertCreated = false;
        boolean recovered = false;
        NdcAlertEvent alertEvent = null;

        if (input.currentNdcUsed().compareTo(input.thresholdPercent()) >= 0) {
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
                    "NDC notification threshold breached",
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

        return new Output(
            previousState,
            state.getCurrentState(),
            state.getBreachCycleNo(),
            alertCreated,
            recovered,
            alertEvent == null ? null : alertEvent.getNdcAlertEventId()
        );
    }
}