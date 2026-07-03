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
import com.thitsaworks.operation_portal.core.hub_services.data.ParticipantBalanceData;
import com.thitsaworks.operation_portal.core.hub_services.data.mapper.ParticipantBalanceDataMapper;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesErrors;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import com.thitsaworks.operation_portal.core.hub_services.query.GetParticipantBalanceByCurrencyIdQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GetParticipantBalanceByCurrencyIdJdbcQueryHandler implements GetParticipantBalanceByCurrencyIdQuery {

    private static final Logger LOG = LoggerFactory.getLogger(GetParticipantBalanceByCurrencyIdJdbcQueryHandler.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GetParticipantBalanceByCurrencyIdJdbcQueryHandler(
            @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws HubServicesException {

        ParticipantBalanceData result;

        try {
            //@@Formatter:off
            final String query = """
                                SELECT pc.currencyId AS currency, la.name AS ledgerAccountType, pp.value, pp.reservedValue, pc.isActive, pp.changedDate FROM participantPosition pp
                                INNER JOIN participantCurrency pc ON pc.participantCurrencyId = pp.participantCurrencyId
                                LEFT JOIN ledgerAccountType la ON la.ledgerAccountTypeId = pc.ledgerAccountTypeId
                                WHERE pp.participantCurrencyId = ?
                                """;
            //@@Formatter:on
            result = this.jdbcTemplate.queryForObject(query,
                                                      new ParticipantBalanceDataMapper(),
                                                      input.getParticipantCurrencyId());

        } catch (Exception e) {

            throw new HubServicesException(HubServicesErrors.HUB_PARTICIPANT_BALANCE_ERROR.description(e.getMessage()));
        }

        return new Output(result);
    }

}
