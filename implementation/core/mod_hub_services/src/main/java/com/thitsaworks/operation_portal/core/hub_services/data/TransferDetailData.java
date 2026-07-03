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
package com.thitsaworks.operation_portal.core.hub_services.data;

public record TransferDetailData(

        String transferId,

        String quoteId,

        String transferState,

        String transferType,

        String subScenario,

        String currency,

        String amountType,

        String quoteAmount,

        String transferAmount,

        String payeeReceivedAmount,

        String payerDfspFee,

        String schemeFee,

        String payeeDfspFee,

        String payeeDfspCommission,

        String submittedOnDate,

        String windowId,

        String settlementId,

        PartyInfoData payerInformation,

        PartyInfoData payeeInformation,

        TransferErrorInfo transferErrorInfo
) {

    public record PartyInfoData(

            String idType,

            String idValue,

            String dfspId,

            String name) {}

    public record TransferErrorInfo(

            String errorCode,

            String errorDescription) {}

}


