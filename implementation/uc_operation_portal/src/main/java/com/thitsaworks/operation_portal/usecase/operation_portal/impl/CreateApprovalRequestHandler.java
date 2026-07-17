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

package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.common.type.PositionActionType;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.approval.command.CreateApprovalRequestCommand;
import com.thitsaworks.operation_portal.core.approval.command.CreateApprovalRequestFieldDetailCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import com.thitsaworks.operation_portal.core.hub_services.query.GetParticipantBalanceByCurrencyIdQuery;
import com.thitsaworks.operation_portal.core.hub_services.query.GetParticipantLimitByCurrencyIdQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDC;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantNDCQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateApprovalRequest;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ActionMetadata(category = ActionCategory.APPROVAL_WORKFLOW)
public class CreateApprovalRequestHandler
    extends OperationPortalAuditableUseCase<CreateApprovalRequest.Input, CreateApprovalRequest.Output>
    implements CreateApprovalRequest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateApprovalRequestHandler.class);

    private final CreateApprovalRequestCommand createApprovalRequestCommand;

    private final CreateApprovalRequestFieldDetailCommand createApprovalRequestFieldDetailCommand;

    private final ParticipantNDCQuery participantNDCQuery;

    private final GetParticipantBalanceByCurrencyIdQuery getParticipantValueByCurrencyIdQuery;

    private final GetParticipantLimitByCurrencyIdQuery getParticipantLimitByCurrencyIdQuery;

    public CreateApprovalRequestHandler(CreateInputAuditCommand createInputAuditCommand,
                                        CreateOutputAuditCommand createOutputAuditCommand,
                                        CreateExceptionAuditCommand createExceptionAuditCommand,
                                        ObjectMapper objectMapper,
                                        PrincipalCache principalCache,
                                        CreateApprovalRequestCommand createApprovalRequestCommand,
                                        CreateApprovalRequestFieldDetailCommand createApprovalRequestFieldDetailCommand,
                                        ParticipantNDCQuery participantNDCQuery,
                                        GetParticipantBalanceByCurrencyIdQuery getParticipantValueByCurrencyIdQuery,
                                        GetParticipantLimitByCurrencyIdQuery getParticipantLimitByCurrencyIdQuery,
                                        ActionAuthorizationManager actionAuthorizationManager) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.createApprovalRequestCommand = createApprovalRequestCommand;
        this.createApprovalRequestFieldDetailCommand = createApprovalRequestFieldDetailCommand;
        this.participantNDCQuery = participantNDCQuery;
        this.getParticipantValueByCurrencyIdQuery = getParticipantValueByCurrencyIdQuery;
        this.getParticipantLimitByCurrencyIdQuery = getParticipantLimitByCurrencyIdQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var output = this.createApprovalRequestCommand.execute(
            new CreateApprovalRequestCommand.Input(
                input.requestedAction(), input.participant(), input.participantCurrency(),
                input.participantSettlementCurrencyId(), input.participantPositionCurrencyId(),
                input.amount(), input.requestedBy()));

        if (PositionActionType.UPDATE_NDC_PERCENTAGE.name().equals(input.requestedAction())) {
            this.createUpdateNdcPercentageFieldDetail(input, output);
        }

        if (PositionActionType.UPDATE_NDC_FIXED.name().equals(input.requestedAction())) {
            this.createUpdateNdcFixedFieldDetail(input, output);
        }

        if (PositionActionType.DEPOSIT.name().equals(input.requestedAction()) ||
                PositionActionType.WITHDRAW.name().equals(input.requestedAction())) {
            this.createBalanceFieldDetail(input, output);
        }

        return new Output(output.approvalRequestId());
    }

    private void createUpdateNdcPercentageFieldDetail(Input input,
                                                      CreateApprovalRequestCommand.Output output) {

        var beforeValue = this.participantNDCQuery
                              .get(input.participant(), input.participantCurrency())
                              .map(ParticipantNDC::getNdcPercent)
                              .map(BigDecimal::toPlainString)
                              .orElse(null);
        var afterValue = input.amount() == null ? null : input.amount().toPlainString();

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), "ndc_percent", "NDC Percent", null, beforeValue,
                afterValue, "PERCENT", 1, ApprovalTabCode.AMOUNT.name()));
    }

    private void createUpdateNdcFixedFieldDetail(Input input,
                                                 CreateApprovalRequestCommand.Output output)
        throws HubServicesException {

        var participantLimitInfo = this.getParticipantLimitByCurrencyIdQuery.execute(
            new GetParticipantLimitByCurrencyIdQuery.Input(
                input.participant(),
                input.participantCurrency()));
        var beforeValue = participantLimitInfo == null ||
                              participantLimitInfo.getParticipantLimitData() == null ||
                              participantLimitInfo.getParticipantLimitData().value() == null ?
                              null : participantLimitInfo
                                         .getParticipantLimitData()
                                         .value()
                                         .toPlainString();
        var afterValue = input.amount() == null ? null : input.amount().toPlainString();

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), "ndc_amount", "NDC Amount", null, beforeValue,
                afterValue, "AMOUNT", 1, ApprovalTabCode.AMOUNT.name()));
    }

    private void createBalanceFieldDetail(Input input, CreateApprovalRequestCommand.Output output)
        throws HubServicesException {

        var participantSettlementCurrencyId = Integer.parseInt(
            input.participantSettlementCurrencyId());
        var participantBalanceInfo = this.getParticipantValueByCurrencyIdQuery.execute(
            new GetParticipantBalanceByCurrencyIdQuery.Input(participantSettlementCurrencyId));

        var beforeValue = participantBalanceInfo.getParticipantBalanceData().value().abs();
        if (input.amount() == null) {
            return;
        }

        var isDepositAction = PositionActionType.DEPOSIT.name().equals(input.requestedAction());
        var afterValue = isDepositAction ? beforeValue.add(input.amount()) :
                             beforeValue.subtract(input.amount());
        var fieldKey = isDepositAction ? "deposit_balance" : "withdraw_balance";
        var fieldLabel = isDepositAction ? "Deposit Balance" : "Withdraw balance";

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, null, beforeValue.toPlainString(),
                afterValue.toPlainString(), "AMOUNT", 1, ApprovalTabCode.AMOUNT.name()));
    }

}
