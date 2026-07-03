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
package com.thitsaworks.operation_portal.core.hub_services.data.mapper;

import com.thitsaworks.operation_portal.core.hub_services.data.TransferDetailData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferDetailDataMapper implements RowMapper<TransferDetailData> {

    @Override
    public TransferDetailData mapRow(ResultSet rs, int rowNum) throws SQLException {

        TransferDetailData.PartyInfoData payerInformation = new TransferDetailData.PartyInfoData(
                rs.getString("payerIdType"),
                rs.getString("payerIdValue"),
                rs.getString("payerDspId"),
                rs.getString("payerName")
        );

        TransferDetailData.PartyInfoData payeeInformation = new TransferDetailData.PartyInfoData(
                rs.getString("payeeIdType"),
                rs.getString("payeeIdValue"),
                rs.getString("payeeDspId"),
                rs.getString("payeeName")
        );

        TransferDetailData.TransferErrorInfo errorInfo = new TransferDetailData.TransferErrorInfo(
                rs.getString("errorCode"),
                rs.getString("errorDescription")
        );

        return new TransferDetailData(
                rs.getString("transferId"),
                rs.getString("quoteId"),
                rs.getString("transferState"),
                rs.getString("transferType"),
                rs.getString("subScenario"),
                rs.getString("currency"),
                rs.getString("amountType"),
                rs.getString("quoteAmount"),
                rs.getString("transferAmount"),
                rs.getString("payeeReceivedAmount"),
                rs.getString("payerDfspFeeAmount"),
                rs.getString("schemeFeeAmount"),
                rs.getString("payeeDfspFeeAmount"),
                rs.getString("payeeDfspCommissionAmount"),
                rs.getString("submittedOnDate"),
                rs.getString("windowId"),
                rs.getString("settlementId"),
                payerInformation,
                payeeInformation,
                errorInfo
        );

    }

}
