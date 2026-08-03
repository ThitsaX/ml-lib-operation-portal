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

import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransactionDetail;

import java.io.Serializable;
import java.math.BigDecimal;

public record RevenueTransactionDetailData(String revenueTransactionDetailId,
                                           String taxCode,
                                           String taxDescription,
                                           BigDecimal taxAmount,
                                           BigDecimal taxAmountCh,
                                           BigDecimal calculatedAmount,
                                           String category,
                                           String responsibleMinistryCode,
                                           String thirdPartyCode,
                                           BigDecimal golPercentage,
                                           BigDecimal golAmount,
                                           BigDecimal ministryPercent,
                                           BigDecimal ministryAmount,
                                           BigDecimal thirdPartyPercent,
                                           BigDecimal thirdPartyAmount,
                                           BigDecimal sendingDfspCommissionPercent,
                                           BigDecimal sendingDfspCommissionAmount,
                                           Long createdDate,
                                           Long updatedDate) implements Serializable {

    public RevenueTransactionDetailData(RevenueTransactionDetail detail) {

        this(detail.getRevenueTransactionDetailId().getId().toString(),
             detail.getTaxCode(),
             detail.getTaxDescription(),
             detail.getTaxAmount(),
             detail.getTaxAmountCh(),
             detail.getCalculatedAmount(),
             detail.getCategory(),
             detail.getResponsibleMinistryCode(),
             detail.getThirdPartyCode(),
             detail.getGolPercentage(),
             detail.getGolAmount(),
             detail.getMinistryPercent(),
             detail.getMinistryAmount(),
             detail.getThirdPartyPercent(),
             detail.getThirdPartyAmount(),
             detail.getSendingDfspCommissionPercent(),
             detail.getSendingDfspCommissionAmount(),
             detail.getCreatedAt().getEpochSecond(),
             detail.getUpdatedAt().getEpochSecond());
    }
}
