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
import com.thitsaworks.operation_portal.core.revenue_transaction.command.ModifyRevenueTransactionCommand;
import com.thitsaworks.operation_portal.core.revenue_transaction.exception.RevenueTransactionErrors;
import com.thitsaworks.operation_portal.core.revenue_transaction.exception.RevenueTransactionException;
import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransaction;
import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransactionDetail;
import com.thitsaworks.operation_portal.core.revenue_transaction.repository.RevenueTransactionDetailRepository;
import com.thitsaworks.operation_portal.core.revenue_transaction.repository.RevenueTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ModifyRevenueTransactionCommandHandler implements ModifyRevenueTransactionCommand {

    private final RevenueTransactionRepository revenueTransactionRepository;
    private final RevenueTransactionDetailRepository revenueTransactionDetailRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws RevenueTransactionException {

        RevenueTransaction revenueTransaction = this.revenueTransactionRepository
                .findById(input.revenueTransactionId())
                .orElseThrow(() -> new RevenueTransactionException(
                        RevenueTransactionErrors.REVENUE_TRANSACTION_NOT_FOUND.format(
                                input.revenueTransactionId().toString())));

        if (!input.hubTransactionId().equals(revenueTransaction.getHubTransactionId()) &&
                this.revenueTransactionRepository.findByHubTransactionId(input.hubTransactionId()).isPresent()) {
            throw new RevenueTransactionException(
                    RevenueTransactionErrors.REVENUE_TRANSACTION_ALREADY_REGISTERED.format(input.hubTransactionId()));
        }

        revenueTransaction.hubTransactionId(input.hubTransactionId())
                          .state(input.state());

        var updatedDetails = new ArrayList<RevenueTransactionDetail>();
        if (input.transactionDetails() != null) {
            if (input.transactionDetails().size() != revenueTransaction.getTransactionDetails().size()) {
                throw new IllegalStateException(
                        "Revenue transaction detail count mismatch. Expected "
                        + revenueTransaction.getTransactionDetails().size()
                        + " but received " + input.transactionDetails().size());
            }

            input.transactionDetails().forEach(update -> {
                var details = revenueTransaction.getTransactionDetails().stream()
                    .filter(detail -> detail.getRevenueTransactionDetailId().getId().toString()
                            .equals(update.revenueTransactionDetailId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Revenue transaction detail not found: " + update.revenueTransactionDetailId()));

                updatedDetails.add(details.revenueSplit(
                        update.calculatedAmount(),
                        update.taxCodeDescription(),
                        update.category(),
                        update.responsibleMinistryCode(),
                        update.thirdPartyCode(),
                        update.golPercentage(),
                        update.golAmount(),
                        update.ministryPercentage(),
                        update.ministryAmount(),
                        update.thirdPartyPercentage(),
                        update.thirdPartyAmount(),
                        update.sendingDfspPercentage(),
                        update.sendingDfspAmount()));
            });
        }

        this.revenueTransactionRepository.saveAndFlush(revenueTransaction);
        this.revenueTransactionDetailRepository.saveAllAndFlush(updatedDetails);

        return new Output(true, revenueTransaction.getRevenueTransactionId());
    }
}
