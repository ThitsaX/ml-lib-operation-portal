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
package com.thitsaworks.operation_portal.core.hub_services.query.impl.jdbc;

import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import com.thitsaworks.operation_portal.core.hub_services.data.mapper.NdcLedgerDataMapper;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesErrors;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcLedgerDataQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GetNdcLedgerDataJdbcQueryHandler
    implements GetNdcLedgerDataQuery {

    private final JdbcTemplate jdbcTemplate;

    public GetNdcLedgerDataJdbcQueryHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE)
        JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws HubServicesException {

        try {
            if (input.participantCurrencyIds() == null || input.participantCurrencyIds().isEmpty()) {
                return new Output(Collections.emptyList());
            }

            String participantCurrencyPlaceholders =
                String.join(", ", Collections.nCopies(input.participantCurrencyIds().size(), "?"));

            String sql = """
                SELECT
                    pc.participantCurrencyId AS participant_currency_id,
                    p.name AS participant_name,
                    pc.currencyId AS currency,
                    IFNULL(position_totals.current_balance, 0) AS current_balance,
                    IFNULL(limit_totals.ndc_limit_amount, 0) AS ndc_limit_amount,
                    pc.isActive AS is_active
                FROM participant p
                JOIN participantCurrency pc
                    ON pc.participantId = p.participantId
                    AND pc.ledgerAccountTypeId = 1
                    AND pc.isActive = 1
                LEFT JOIN (
                    SELECT
                        participantCurrencyId,
                        TRUNCATE(SUM(IFNULL(value, 0)), 2) AS current_balance
                    FROM participantPosition
                    GROUP BY participantCurrencyId
                ) position_totals
                    ON position_totals.participantCurrencyId = pc.participantCurrencyId
                LEFT JOIN (
                    SELECT
                        pl.participantCurrencyId,
                        SUM(IFNULL(pl.value, 0)) AS ndc_limit_amount
                    FROM participantLimit pl
                    JOIN participantLimitType plt
                        ON plt.participantLimitTypeId = pl.participantLimitTypeId
                        AND plt.name = 'NET_DEBIT_CAP'
                    WHERE pl.isActive = 1
                    GROUP BY pl.participantCurrencyId
                ) limit_totals
                    ON limit_totals.participantCurrencyId = pc.participantCurrencyId
                WHERE pc.participantCurrencyId IN (%s)
                GROUP BY
                    p.participantId,
                    pc.participantCurrencyId,
                    p.name,
                    pc.currencyId,
                    pc.isActive,
                    position_totals.current_balance,
                    limit_totals.ndc_limit_amount
                ORDER BY
                    p.name,
                    pc.currencyId
                """.formatted(participantCurrencyPlaceholders);

            var data = jdbcTemplate.query(
                sql,
                new NdcLedgerDataMapper(),
                input.participantCurrencyIds().toArray()
                                         );

            return new Output(data);

        } catch (Exception exception) {
            throw new HubServicesException(
                HubServicesErrors.HUB_PARTICIPANT_POSITION_ERROR
                    .description(exception.getMessage())
            );
        }
    }
}
