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

import com.thitsaworks.operation_portal.core.hub_services.data.TransferData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferDataMapper implements RowMapper<TransferData> {

    @Override
    public TransferData mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new TransferData(
                rs.getString("transferId"),
                rs.getString("state"),
                rs.getString("type"),
                rs.getString("currency"),
                rs.getBigDecimal("amount"),
                rs.getString("payer_dfsp"),
                rs.getString("payer_dfsp_name"),
                rs.getString("payee_dfsp"),
                rs.getString("payee_dfsp_name"),
                rs.getString("window_id"),
                rs.getString("settlement_batch"),
                rs.getString("submitted_on_date")
        );

    }

}
