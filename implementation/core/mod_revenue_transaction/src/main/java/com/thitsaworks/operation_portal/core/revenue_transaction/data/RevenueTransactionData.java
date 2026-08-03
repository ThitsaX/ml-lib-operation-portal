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
package com.thitsaworks.operation_portal.core.revenue_transaction.data;

import com.thitsaworks.operation_portal.component.common.type.TransactionState;
import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record RevenueTransactionData(String revenueTransactionId,
                                     String hubTransactionId,
                                     String tin,
                                     String taxPayerName,
                                     String billNumber,
                                     String billDate,
                                     BigDecimal totalAmount,
                                     String amountCurrency,
                                     String sentCurrency,
                                     BigDecimal rateExchange,
                                     String receiptNumber,
                                     String senderDfspId,
                                     TransactionState state,
                                     Long createdDate,
                                     Long updatedDate,
                                     List<RevenueTransactionDetailData> transactionDetails)
        implements Serializable {

    public RevenueTransactionData(RevenueTransaction transaction) {

        this(transaction.getRevenueTransactionId().getId().toString(),
             transaction.getHubTransactionId(),
             transaction.getTin(),
             transaction.getTaxPayerName(),
             transaction.getBillNumber(),
             transaction.getBillDate(),
             transaction.getTotalAmount(),
             transaction.getAmountCurrency(),
             transaction.getSentCurrency(),
             transaction.getRateExchange(),
             transaction.getReceiptNumber(),
             transaction.getSenderDfspId(),
             transaction.getState(),
             transaction.getCreatedAt().getEpochSecond(),
             transaction.getUpdatedAt().getEpochSecond(),
             transaction.getTransactionDetails().stream().map(RevenueTransactionDetailData::new).toList());
    }
}
