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
import com.thitsaworks.operation_portal.core.hub_services.data.CurrencyData;
import com.thitsaworks.operation_portal.core.hub_services.data.mapper.CurrencyDataMapper;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesErrors;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import com.thitsaworks.operation_portal.core.hub_services.query.GetCurrentParticipantCurrenciesQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetCurrentParticipantCurrenciesJdbcQueryHandler implements GetCurrentParticipantCurrenciesQuery {

    private static final Logger LOG = LoggerFactory.getLogger(GetCurrentParticipantCurrenciesJdbcQueryHandler.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GetCurrentParticipantCurrenciesJdbcQueryHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) {

        List<CurrencyData> results;
        try {

            results = this.jdbcTemplate.query(
                "SELECT currencyId FROM participantCurrency\n" +
                    "INNER JOIN participant\n" +
                    " ON participant.participantId =participantCurrency.participantId\n" +
                    " WHERE participantCurrency.isActive =1 and name=? GROUP BY currencyId ORDER BY currencyId",
                new CurrencyDataMapper(), input.getDfspId());

        } catch (Exception e) {
            try {
                throw new HubServicesException(HubServicesErrors.HUB_PARTICIPANT_ERROR.description(e.getMessage()));

            } catch (HubServicesException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (results == null || results.isEmpty()) {

            return new Output(new ArrayList<>());
        }

        return new Output(results);
    }

}
