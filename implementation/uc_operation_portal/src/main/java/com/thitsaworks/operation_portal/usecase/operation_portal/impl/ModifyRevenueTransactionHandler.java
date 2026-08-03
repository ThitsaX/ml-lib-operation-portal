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
import com.thitsaworks.operation_portal.component.common.identifier.RevenueTransactionId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_config.engine.RevenueEngine;
import com.thitsaworks.operation_portal.core.revenue_transaction.command.ModifyRevenueTransactionCommand;
import com.thitsaworks.operation_portal.core.revenue_transaction.query.RevenueTransactionQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenueTransaction;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_TRANSACTION)
public class ModifyRevenueTransactionHandler
        extends OperationPortalAuditableUseCase<ModifyRevenueTransaction.Input, ModifyRevenueTransaction.Output>
        implements ModifyRevenueTransaction {

    private final ModifyRevenueTransactionCommand modifyRevenueTransactionCommand;
    private final RevenueTransactionQuery revenueTransactionQuery;
    private final RevenueEngine revenueEngine;

    public ModifyRevenueTransactionHandler(CreateInputAuditCommand createInputAuditCommand,
                                           CreateOutputAuditCommand createOutputAuditCommand,
                                           CreateExceptionAuditCommand createExceptionAuditCommand,
                                           ObjectMapper objectMapper,
                                           PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           ModifyRevenueTransactionCommand modifyRevenueTransactionCommand,
                                           RevenueTransactionQuery revenueTransactionQuery,
                                           RevenueEngine revenueEngine) {

        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
              objectMapper, principalCache, actionAuthorizationManager);

        this.modifyRevenueTransactionCommand = modifyRevenueTransactionCommand;
        this.revenueTransactionQuery = revenueTransactionQuery;
        this.revenueEngine = revenueEngine;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var calculatedDetails = this.calculateTransactionDetails(input.revenueTransactionId());
        var detailUpdates = calculatedDetails.stream()
                .map(calculated -> {
                    var detail = calculated.detail();
                    return new ModifyRevenueTransactionCommand.TransactionDetail(
                            calculated.revenueTransactionDetailId(),
                            calculated.calculatedAmount(),
                            detail.category(),
                            detail.responsibleMinistryCode(),
                            detail.thirdPartyCode(),
                            detail.golPercent(),
                            detail.golAmount(),
                            detail.ministryPercent(),
                            detail.ministryAmount(),
                            detail.thirdPartyPercent(),
                            detail.thirdPartyAmount(),
                            detail.sendingDfspCommissionPercent(),
                            detail.sendingDfspCommissionAmount());
                })
                .toList();
        var output = this.modifyRevenueTransactionCommand.execute(new ModifyRevenueTransactionCommand.Input(
            input.revenueTransactionId(), input.hubTransactionId(), input.state(), detailUpdates));
        var transactionDetails = calculatedDetails.stream().map(CalculatedTransactionDetail::detail).toList();

        return new Output(output.modified(), output.revenueTransactionId(), transactionDetails);
    }

    private List<CalculatedTransactionDetail> calculateTransactionDetails(
            RevenueTransactionId revenueTransactionId) throws DomainException {

        var revenueTransaction = this.revenueTransactionQuery.get(revenueTransactionId);
        var transactionDetails = new ArrayList<CalculatedTransactionDetail>();

        for (var detail : revenueTransaction.transactionDetails()) {

            var amount = revenueTransaction.sentCurrency() == "USD" ? detail.taxAmount() : detail.taxAmountCh();
            
            var revenueSplit = this.revenueEngine.calculateRevenue(detail.taxCode(), amount);

            transactionDetails.add(new CalculatedTransactionDetail(
                    detail.revenueTransactionDetailId(),
                    amount,
                    new ModifyRevenueTransaction.TransactionDetail(
                        amount,
                            revenueSplit.revenueConfigCategory().name(),
                            revenueSplit.responsibleMinistryCode(),
                            revenueSplit.thirdPartyProviderCode(),
                            revenueSplit.golPercentage(),
                            revenueSplit.golAmount(),
                            revenueSplit.ministryPercentage(),
                            revenueSplit.ministryAmount(),
                            revenueSplit.thirdPartyPercentage(),
                            revenueSplit.thirdPartyAmount(),
                            revenueSplit.sendingDfspPercentage(),
                            revenueSplit.sendingDfspAmount())));
        }

        return transactionDetails;
    }

    private record CalculatedTransactionDetail(
            String revenueTransactionDetailId,
            BigDecimal calculatedAmount,
            ModifyRevenueTransaction.TransactionDetail detail) {
    }
}
