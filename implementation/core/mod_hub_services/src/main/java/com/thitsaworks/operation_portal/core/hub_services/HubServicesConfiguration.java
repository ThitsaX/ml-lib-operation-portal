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
package com.thitsaworks.operation_portal.core.hub_services;

import com.thitsaworks.operation_portal.component.infra.mysql.hub.HubJdbcPersistenceConfiguration;
import com.thitsaworks.operation_portal.component.infra.mongo.ReportingMongoConfiguration;
import com.thitsaworks.operation_portal.component.misc.MiscConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import(value = {HubJdbcPersistenceConfiguration.class, MiscConfiguration.class, ReportingMongoConfiguration.class})
@ComponentScan("com.thitsaworks.operation_portal.core.hub_services")
public class HubServicesConfiguration {

    @Bean
    public HubServicesConfiguration.Settings hubServiceConfigurationSettings() {

        return new HubServicesConfiguration.Settings(System.getProperty("CENTRAL_LEDGER_ENDPOINT"),
                                                     System.getProperty("SETTLEMENT_ENDPOINT"
                                                                            ));

    }

    public record Settings(String centralLedgerEndpoint, String settlementEndpoint) {}

}
