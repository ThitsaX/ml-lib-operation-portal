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

import com.thitsaworks.operation_portal.core.hub_services.data.WindowInfoData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class WindowInfoDataMapper implements RowMapper<WindowInfoData> {

    @Override
    public WindowInfoData mapRow(ResultSet rs, int rowNum) throws SQLException {

        WindowInfoData.WindowInfoDataBuilder
            builder =
            WindowInfoData.builder()
                          .DfspName(rs.getString("DfspName"))
                          .Debit(rs.getBigDecimal("Debit"))
                          .Credit(rs.getBigDecimal("Credit"))
                          .currencyId(rs.getString("currencyId"))
                          .windowOpenedDate(rs.getString("WindowOpenDate"))
                          .windowClosedDate(rs.getString("WindowSettledDate"));

        // Safely check if the column exists before trying to access it
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if ("WindowIDs".equalsIgnoreCase(metaData.getColumnName(i))) {
                builder.settlementWindowIds(rs.getString("WindowIDs"));
                break;
            }
        }

        return builder.build();
    }

}