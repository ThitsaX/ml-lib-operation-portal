package com.thitsaworks.operation_portal.core.notification.command;

import com.thitsaworks.operation_portal.component.common.identifier.NdcAlertEventId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface EvaluateNdcThresholdCommand {

    record Input(
        ParticipantNDCId participantNDCId,
        String participantName,
        String currency,
        BigDecimal currentBalance,
        BigDecimal currentNdcUsed,
        BigDecimal thresholdPercent,
        LocalDateTime evaluatedAt,
        String actor
    ) {}

    record Output(
        NdcThresholdStateType previousState,
        NdcThresholdStateType currentState,
        long breachCycleNo,
        boolean alertCreated,
        boolean recovered,
        NdcAlertEventId alertEventId
    ) {}

    Output execute(Input input) throws DomainException;
}