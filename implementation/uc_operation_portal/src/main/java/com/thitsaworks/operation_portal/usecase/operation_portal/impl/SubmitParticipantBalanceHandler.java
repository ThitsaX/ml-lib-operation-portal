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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.type.PositionActionType;
import com.thitsaworks.operation_portal.component.fspiop.model.ExtensionList;
import com.thitsaworks.operation_portal.component.fspiop.model.Money;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.component.misc.util.TransferIdGenerator;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.hub_services.ParticipantHubClient;
import com.thitsaworks.operation_portal.core.hub_services.api.PostParticipantBalance;
import com.thitsaworks.operation_portal.core.hub_services.api.PutUpdateParticipantLimit;
import com.thitsaworks.operation_portal.core.hub_services.data.ParticipantBalanceData;
import com.thitsaworks.operation_portal.core.hub_services.data.ParticipantPositionData;
import com.thitsaworks.operation_portal.core.hub_services.query.GetParticipantBalanceByCurrencyIdQuery;
import com.thitsaworks.operation_portal.core.hub_services.query.GetParticipantLimitByCurrencyIdQuery;
import com.thitsaworks.operation_portal.core.hub_services.query.GetParticipantPositionsDataByParticipantNameAndCurrencyQuery;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementAction;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.command.CreateParticipantNDCHistoryCommand;
import com.thitsaworks.operation_portal.core.participant.command.ModifyParticipantNDCCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDC;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantNDCQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.SubmitParticipantBalance;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.jobs.NdcThresholdWorker;
import com.thitsaworks.operation_portal.usecase.util.Utility;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION)
public class SubmitParticipantBalanceHandler
    extends OperationPortalAuditableUseCase<SubmitParticipantBalance.Input, SubmitParticipantBalance.Output>
    implements SubmitParticipantBalance {

    private static final Logger LOG = LoggerFactory.getLogger(SubmitParticipantBalanceHandler.class);

    private final ObjectMapper objectMapper;

    private final ParticipantHubClient participantHubClient;

    private final Utility utility;

    private final GetParticipantPositionsDataByParticipantNameAndCurrencyQuery participantPositionsQuery;

    private final GetParticipantBalanceByCurrencyIdQuery participantBalanceQuery;

    private final GetParticipantLimitByCurrencyIdQuery participantLimitQuery;

    private final ParticipantNDCQuery participantNDCQuery;

    private final CreateParticipantNDCHistoryCommand createParticipantNDCHistoryCommand;

    private final ModifyParticipantNDCCommand modifyParticipantNDCCommand;

    private final NdcThresholdWorker ndcThresholdWorker;

    public SubmitParticipantBalanceHandler(
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ObjectMapper objectMapper,
        PrincipalCache principalCache,
        ActionAuthorizationManager actionAuthorizationManager,
        ParticipantHubClient participantHubClient,
        Utility utility,
        GetParticipantPositionsDataByParticipantNameAndCurrencyQuery participantPositionsQuery,
        GetParticipantBalanceByCurrencyIdQuery participantBalanceQuery,
        GetParticipantLimitByCurrencyIdQuery participantLimitQuery,
        ParticipantNDCQuery participantNDCQuery,
        CreateParticipantNDCHistoryCommand createParticipantNDCHistoryCommand,
        ModifyParticipantNDCCommand modifyParticipantNDCCommand,
        NdcThresholdWorker ndcThresholdWorker) {

        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
              objectMapper, principalCache, actionAuthorizationManager);

        this.objectMapper = objectMapper;
        this.participantHubClient = participantHubClient;
        this.utility = utility;
        this.participantPositionsQuery = participantPositionsQuery;
        this.participantBalanceQuery = participantBalanceQuery;
        this.participantLimitQuery = participantLimitQuery;
        this.participantNDCQuery = participantNDCQuery;
        this.createParticipantNDCHistoryCommand = createParticipantNDCHistoryCommand;
        this.modifyParticipantNDCCommand = modifyParticipantNDCCommand;
        this.ndcThresholdWorker = ndcThresholdWorker;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, JsonProcessingException {

        String participantName = input.participantId();
        String currency = input.currency()
                               .toString();

        ParticipantPositionData participantAccounts = this.resolveParticipantAccounts(
            participantName, currency);

        int settlementAccountId = participantAccounts.participantSettlementCurrencyId();
        int positionAccountId = participantAccounts.participantPositionCurrencyId();

        LOG.info(
            "Resolved participant accounts : participantName : {}, currency : {}, settlementAccountId : {}, positionAccountId : {}",
            participantName, currency, settlementAccountId, positionAccountId);

        String mojaloopAction = switch (input.action()) {
            case DEPOSIT -> SettlementAction.recordFundsIn.name();
            case WITHDRAW -> SettlementAction.recordFundsOutPrepareReserve.name();
            default -> throw new ParticipantException(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_ACTION);
        };

        Money money = new Money()
                          .currency(input.currency())
                          .amount(this.toMojaloopAmount(input.amount()));

        if (input.action() == PositionActionType.WITHDRAW) {
            this.validateWithdrawal(participantName, currency, settlementAccountId,
                                    positionAccountId, input.amount());
        }

        LOG.info("Get ParticipantNDC Query Request : participantName : {}, currency : {}",
                 participantName, currency);

        Optional<ParticipantNDC> ndcData = this.participantNDCQuery.get(participantName, currency);

        LOG.info("Get ParticipantNDC Query Response : {}", ndcData);

        BigDecimal ndcPercent = ndcData.map(ParticipantNDC::getNdcPercent)
                                       .orElse(BigDecimal.ZERO);
        boolean recalculateNDC = ndcPercent.compareTo(BigDecimal.ZERO) != 0;

        String transferId = TransferIdGenerator.generateTransferId();
        String requestUserEmail = this.utility.getEmail(input.requestedBy());
        String reason = input.action() == PositionActionType.DEPOSIT ? "Deposit" : "Withdrawal";
        ExtensionList extensionList = input.extensionList();

        PostParticipantBalance.Request hubRequest = new PostParticipantBalance.Request(
            transferId, requestUserEmail, mojaloopAction, reason, money, extensionList);

        LOG.info(
            "Post Participant Balance Request from external API to Mojaloop : participantName : {}, settlementAccountId : {}, request : {}",
            participantName, settlementAccountId, this.objectMapper.writeValueAsString(hubRequest));

        PostParticipantBalance.Response hubResponse = this.participantHubClient.postParticipantBalance(
            participantName, String.valueOf(settlementAccountId), hubRequest);

        LOG.info("Post Participant Balance Response from Mojaloop : {}",
                 this.objectMapper.writeValueAsString(hubResponse));

        if (recalculateNDC) {
            this.recalculatePercentageNDC(participantName, currency, settlementAccountId,
                                          input.action(), input.amount(), requestUserEmail);
        }

        this.ndcThresholdWorker.executeOnDemand();

        return new Output("ACCEPTED");
    }

    private ParticipantPositionData resolveParticipantAccounts(String participantName,
                                                               String currency)
        throws DomainException {

        LOG.info("Get participant account information : participantName : {}, currency : {}",
                 participantName, currency);

        var output = this.participantPositionsQuery.execute(
            new GetParticipantPositionsDataByParticipantNameAndCurrencyQuery.Input(
                participantName, currency));

        if (output == null || output.getParticipantPositionData() == null ||
                output.getParticipantPositionData()
                      .isEmpty()) {
            throw new ParticipantException(ParticipantErrors.PARTICIPANT_ACCOUNT_NOT_FOUND.format(
                participantName, currency));
        }

        List<ParticipantPositionData> accounts = output.getParticipantPositionData();
        ParticipantPositionData participantAccount = accounts.stream()
                                                             .filter(account -> currency.equalsIgnoreCase(
                                                                 account.currency()))
                                                             .findFirst()
                                                             .orElseThrow(() ->
                                                                 new ParticipantException(
                                                                     ParticipantErrors.PARTICIPANT_ACCOUNT_NOT_FOUND.format(
                                                                         participantName,
                                                                         currency)));

        if (!participantAccount.isActive()) {
            throw new ParticipantException(ParticipantErrors.PARTICIPANT_ACCOUNT_INACTIVE.format(
                participantName, currency));
        }

        if (participantAccount.participantSettlementCurrencyId() == null ||
                participantAccount.participantPositionCurrencyId() == null) {
            throw new ParticipantException(ParticipantErrors.INVALID_PARTICIPANT_ACCOUNT_DATA.format(
                participantName, currency));
        }

        return participantAccount;
    }

    private void validateWithdrawal(String participantName,
                                    String currency,
                                    int settlementAccountId,
                                    int positionAccountId,
                                    BigDecimal withdrawalAmount)
        throws DomainException {

        var participantLimitInfo = this.participantLimitQuery.execute(
            new GetParticipantLimitByCurrencyIdQuery.Input(participantName, currency));

        var participantBalanceInfo = this.participantBalanceQuery.execute(
            new GetParticipantBalanceByCurrencyIdQuery.Input(settlementAccountId));

        var participantPositionInfo = this.participantBalanceQuery.execute(
            new GetParticipantBalanceByCurrencyIdQuery.Input(positionAccountId));

        if (participantLimitInfo == null || participantLimitInfo.getParticipantLimitData() == null) {
            throw new ParticipantException(ParticipantErrors.PARTICIPANT_NDC_NOT_FOUND);
        }

        this.validateBalanceQueryResult(participantBalanceInfo, participantName, currency);
        this.validateBalanceQueryResult(participantPositionInfo, participantName, currency);

        BigDecimal currentBalance = participantBalanceInfo.getParticipantBalanceData()
                                                          .value()
                                                          .abs();
        BigDecimal currentParticipantLimit = participantLimitInfo.getParticipantLimitData()
                                                                    .value()
                                                                    .abs();
        BigDecimal participantPosition = participantPositionInfo.getParticipantBalanceData()
                                                                  .value();

        if (withdrawalAmount.compareTo(currentBalance) > 0) {
            throw new ParticipantException(ParticipantErrors.INSUFFICIENT_BALANCE);
        }

        BigDecimal remainingBalance = currentBalance.subtract(withdrawalAmount);

        LOG.info("Get ParticipantNDC Query Request : participantName : {}, currency : {}",
                 participantName, currency);

        Optional<ParticipantNDC> ndcData = this.participantNDCQuery.get(participantName, currency);

        LOG.info("Get ParticipantNDC Query Response : {}", ndcData);

        BigDecimal ndcPercent = ndcData.map(ParticipantNDC::getNdcPercent)
                                       .orElse(BigDecimal.ZERO)
                                       .setScale(2, RoundingMode.DOWN);

        if (ndcPercent.compareTo(BigDecimal.ZERO) == 0) {
            this.validateFixedNdc(remainingBalance, currentParticipantLimit);
        } else {
            this.validatePercentageNdc(remainingBalance, ndcPercent, participantPosition);
        }

        this.validateRemainingBalanceAgainstPosition(remainingBalance, participantPosition);
    }

    private void validateFixedNdc(BigDecimal remainingBalance,
                                  BigDecimal currentParticipantLimit)
        throws ParticipantException {

        if (remainingBalance.compareTo(currentParticipantLimit) < 0) {
            throw new ParticipantException(ParticipantErrors.BALANCE_BELOW_NDC);
        }
    }

    private void validatePercentageNdc(BigDecimal remainingBalance,
                                       BigDecimal ndcPercent,
                                       BigDecimal participantPosition)
        throws ParticipantException {

        BigDecimal projectedNdcLimit = remainingBalance.multiply(ndcPercent)
                                                       .divide(BigDecimal.valueOf(100))
                                                       .abs()
                                                       .setScale(2, RoundingMode.DOWN);

        LOG.info(
            "Validate projected percentage NDC : remainingBalance : {}, ndcPercent : {}, projectedNdcLimit : {}, participantPosition : {}",
            remainingBalance, ndcPercent, projectedNdcLimit, participantPosition);

        if (participantPosition.compareTo(BigDecimal.ZERO) > 0 &&
                projectedNdcLimit.compareTo(participantPosition.abs()) < 0) {
            throw new ParticipantException(ParticipantErrors.NDC_BELOW_CURRENT_POSITION);
        }
    }

    private void validateRemainingBalanceAgainstPosition(BigDecimal remainingBalance,
                                                         BigDecimal participantPosition)
        throws ParticipantException {

        if (participantPosition.compareTo(BigDecimal.ZERO) > 0 &&
                remainingBalance.compareTo(participantPosition.abs()) < 0) {
            throw new ParticipantException(ParticipantErrors.BALANCE_BELOW_CURRENT_POSITION);
        }
    }

    private void recalculatePercentageNDC(String participantName,
                                          String currency,
                                          int settlementAccountId,
                                          PositionActionType action,
                                          BigDecimal transactionAmount,
                                          String madeBy)
        throws DomainException, JsonProcessingException {

        var balanceInfo = this.participantBalanceQuery.execute(
            new GetParticipantBalanceByCurrencyIdQuery.Input(settlementAccountId));

        this.validateBalanceQueryResult(balanceInfo, participantName, currency);

        ParticipantBalanceData balanceData = balanceInfo.getParticipantBalanceData();
        BigDecimal updatedBalance = balanceData.value()
                                               .abs();

        if (action == PositionActionType.DEPOSIT) {
            updatedBalance = updatedBalance.add(transactionAmount);
        } else if (action == PositionActionType.WITHDRAW) {
            updatedBalance = updatedBalance.subtract(transactionAmount);
        }

        updatedBalance = updatedBalance.setScale(2, RoundingMode.DOWN);

        LOG.info("Get ParticipantNDC Query Request : participantName : {}, currency : {}",
                 participantName, currency);

        Optional<ParticipantNDC> optionalNdc = this.participantNDCQuery.get(participantName, currency);

        LOG.info("Get ParticipantNDC Query Response : {}", optionalNdc);

        ParticipantNDC participantNDC = optionalNdc.orElseThrow(() ->
            new ParticipantException(ParticipantErrors.PARTICIPANT_NDC_NOT_FOUND));

        BigDecimal ndcPercent = participantNDC.getNdcPercent()
                                              .setScale(2, RoundingMode.DOWN);
        BigDecimal calculatedNdcLimit = this.calculateNdcLimit(
            balanceData, currency, updatedBalance, ndcPercent);

        PutUpdateParticipantLimit.Request updateLimitRequest =
            new PutUpdateParticipantLimit.Request(
                currency,
                new PutUpdateParticipantLimit.Limit(
                    SettlementAction.NET_DEBIT_CAP.toString(), calculatedNdcLimit, 10));

        LOG.info(
            "Put Update Participant Limit Request from Operation Portal to Mojaloop : participantName : {}, request : {}",
            participantName, this.objectMapper.writeValueAsString(updateLimitRequest));

        this.participantHubClient.putUpdateParticipantLimit(participantName, updateLimitRequest);

        LOG.info("Put Update Participant Limit Response from Mojaloop : [no response]");

        this.createParticipantNDCHistoryCommand.execute(
            new CreateParticipantNDCHistoryCommand.Input(participantNDC));

        this.modifyParticipantNDCCommand.execute(
            new ModifyParticipantNDCCommand.Input(
                participantNDC.getParticipantNDCId(),
                ndcPercent,
                calculatedNdcLimit,
                updatedBalance,
                madeBy));
    }

    private BigDecimal calculateNdcLimit(ParticipantBalanceData balanceData,
                                         String currency,
                                         BigDecimal updatedBalance,
                                         BigDecimal ndcPercent) {

        if (balanceData == null || !currency.equals(balanceData.currency()) ||
                !"SETTLEMENT".equalsIgnoreCase(String.valueOf(balanceData.ledgerAccountType())) ||
                ndcPercent == null || ndcPercent.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return updatedBalance.multiply(ndcPercent)
                             .divide(BigDecimal.valueOf(100))
                             .abs()
                             .setScale(2, RoundingMode.DOWN);
    }

    private void validateBalanceQueryResult(GetParticipantBalanceByCurrencyIdQuery.Output output,
                                            String participantName,
                                            String currency)
        throws ParticipantException {

        if (output == null || output.getParticipantBalanceData() == null) {
            throw new ParticipantException(ParticipantErrors.INVALID_PARTICIPANT_ACCOUNT_DATA.format(
                participantName, currency));
        }
    }

    private String toMojaloopAmount(BigDecimal amount) {

        return amount.stripTrailingZeros()
                     .toPlainString();
    }

}
