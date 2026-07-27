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
import com.thitsaworks.operation_portal.core.revenue_transaction.command.FinalizeRevenueTransactionCommand;
import com.thitsaworks.operation_portal.core.revenue_transaction.exception.RevenueTransactionErrors;
import com.thitsaworks.operation_portal.core.revenue_transaction.exception.RevenueTransactionException;
import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransaction;
import com.thitsaworks.operation_portal.core.revenue_transaction.repository.RevenueTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinalizeRevenueTransactionCommandHandler implements FinalizeRevenueTransactionCommand {

    private final RevenueTransactionRepository revenueTransactionRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws RevenueTransactionException {

        RevenueTransaction revenueTransaction = this.revenueTransactionRepository
                .findById(input.revenueTransactionId())
                .orElseThrow(() -> new RevenueTransactionException(
                        RevenueTransactionErrors.REVENUE_TRANSACTION_NOT_FOUND.format(
                                input.revenueTransactionId().toString())));

        revenueTransaction.receiptNumber(input.receiptNumber())
                          .state(input.state());

        this.revenueTransactionRepository.save(revenueTransaction);

        return new Output(true, revenueTransaction.getRevenueTransactionId());
    }
}
