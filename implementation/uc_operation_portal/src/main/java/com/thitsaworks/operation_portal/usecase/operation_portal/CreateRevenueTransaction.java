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
package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueTransactionId;
import com.thitsaworks.operation_portal.component.common.type.TransactionState;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;
import com.thitsaworks.operation_portal.core.revenue_transaction.data.RevenueTransactionData;
import com.thitsaworks.operation_portal.core.revenue_transaction.data.RevenueTransactionDetailInput;

import java.math.BigDecimal;
import java.util.List;

public interface CreateRevenueTransaction
        extends UseCase<CreateRevenueTransaction.Input, CreateRevenueTransaction.Output> {

    record Input(String hubTransactionId,
                 String tin,
                 String taxPayerName,
                 String billNumber,
                 String billDate,
                 BigDecimal totalAmount,
                 String amountCurrency,
                 String sentCurrency,
                 BigDecimal rateExchange,
                 String senderDfspId,
                 TransactionState state,
                 List<RevenueTransactionDetailInput> transactionDetails) {
    }

    record Output(boolean created,
                  RevenueTransactionId revenueTransactionId,
                  RevenueTransactionData beforeValue,
                  RevenueTransactionData afterValue) {
    }
}
