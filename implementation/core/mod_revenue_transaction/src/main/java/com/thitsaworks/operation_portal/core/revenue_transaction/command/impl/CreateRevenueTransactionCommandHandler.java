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
package com.thitsaworks.operation_portal.core.revenue_transaction.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.revenue_transaction.command.CreateRevenueTransactionCommand;
import com.thitsaworks.operation_portal.core.revenue_transaction.data.RevenueTransactionDetailInput;
import com.thitsaworks.operation_portal.core.revenue_transaction.exception.RevenueTransactionErrors;
import com.thitsaworks.operation_portal.core.revenue_transaction.exception.RevenueTransactionException;
import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransaction;
import com.thitsaworks.operation_portal.core.revenue_transaction.repository.RevenueTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateRevenueTransactionCommandHandler implements CreateRevenueTransactionCommand {

    private final RevenueTransactionRepository revenueTransactionRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws RevenueTransactionException {

        if (this.revenueTransactionRepository.findByHubTransactionId(input.hubTransactionId()).isPresent()) {
            throw new RevenueTransactionException(
                    RevenueTransactionErrors.REVENUE_TRANSACTION_ALREADY_REGISTERED.format(input.hubTransactionId()));
        }

        RevenueTransaction revenueTransaction = new RevenueTransaction(input.hubTransactionId(),
                                                                        input.settlementId(),
                                                                        input.tin(),
                                                                        input.taxPayerName(),
                                                                        input.billNumber(),
                                                                        input.billDate(),
                                                                        input.totalAmount(),
                                                                        input.amountCurrency(),
                                                                        input.rateExchange(),
                                                                        input.senderDfspId(),
                                                                        input.state());

        this.addTransactionDetails(revenueTransaction, input.transactionDetails());
        revenueTransaction = this.revenueTransactionRepository.saveAndFlush(revenueTransaction);

        return new Output(true, revenueTransaction.getRevenueTransactionId());
    }

    private void addTransactionDetails(RevenueTransaction revenueTransaction,
                                       List<RevenueTransactionDetailInput> transactionDetails) {

        if (transactionDetails == null) {
            return;
        }

        transactionDetails.forEach(detail -> revenueTransaction.addTransactionDetail(
                detail.taxCode(),
                detail.taxDescription(),
                detail.taxAmount(),
                detail.taxAmountCh(),
                detail.category(),
                detail.responsibleMinistryCode(),
                detail.thirdPartyCode(),
                detail.golPercentage(),
                detail.golAmount(),
                detail.ministryPercent(),
                detail.ministryAmount(),
                detail.thirdPartyPercent(),
                detail.thirdPartyAmount(),
                detail.sendingDfspCommissionPercent(),
                detail.sendingDfspCommissionAmount()));
    }
}
