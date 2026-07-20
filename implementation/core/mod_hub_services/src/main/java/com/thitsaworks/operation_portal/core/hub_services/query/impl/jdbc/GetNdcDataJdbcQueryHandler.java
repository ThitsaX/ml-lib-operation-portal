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
import com.thitsaworks.operation_portal.core.hub_services.data.mapper.NdcUsedDataMapper;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesErrors;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import com.thitsaworks.operation_portal.core.hub_services.query.GetNdcUsedDataQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GetNdcDataJdbcQueryHandler implements GetNdcUsedDataQuery {

    private static final Logger LOG = LoggerFactory.getLogger(GetNdcDataJdbcQueryHandler.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GetNdcDataJdbcQueryHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws HubServicesException {

        try {
            LOG.debug("Executing Central Ledger NDC used calculation query for DFSP [{}]",
                      input.getFspID());

            String sql = """
                SELECT
                    IFNULL(pc.currencyId, '') AS currency,
                    ROUND(
                        CEIL(
                            (
                                (SUM(IFNULL(pp.value, 0)) / SUM(IFNULL(pl.value, 0))) * 100
                            ) * 100
                        ) / 100,
                        2
                    ) AS ndcUsed,
                    MIN(
                        CASE
                            WHEN pc.ledgerAccountTypeId = 1
                            THEN pc.isActive
                        END
                    ) AS isActive
                FROM participant p
                LEFT JOIN participantCurrency pc
                    ON pc.participantId = p.participantId
                LEFT JOIN participantLimit pl
                    ON pc.participantCurrencyId = pl.participantCurrencyId
                   AND pl.isActive = 1
                LEFT JOIN participantPosition pp
                    ON pp.participantCurrencyId = pc.participantCurrencyId
                   AND pc.ledgerAccountTypeId = 1
                WHERE (? = 'All' OR p.name = ?)
                  AND p.name NOT LIKE '%HUB%'
                GROUP BY
                    p.participantId,
                    p.name,
                    p.description,
                    pc.currencyId
                ORDER BY
                    p.name,
                    pc.currencyId
                """;

            var result = jdbcTemplate.query(
                sql, new NdcUsedDataMapper(), input.getFspID(),
                input.getFspID());

            if (result == null || result.isEmpty()) {

                LOG.debug("Central Ledger NDC used calculation returned no data for DFSP [{}]",
                          input.getFspID());

                return null;
            }

            result.forEach(data ->
                LOG.debug("Central Ledger NDC used result: dfsp={}, currency={}, ndcUsedPercent={}, active={}",
                          input.getFspID(), data.currency(), data.ndcUsed(), data.isActive()));

            return new Output(result);

        } catch (Exception e) {

            LOG.error("Central Ledger NDC used calculation query failed for DFSP [{}]",
                      input.getFspID(), e);

            throw new HubServicesException(
                HubServicesErrors.HUB_PARTICIPANT_POSITION_ERROR.description(e.getMessage()));
        }

    }

}
