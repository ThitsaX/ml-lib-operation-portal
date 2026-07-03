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
package com.thitsaworks.operation_portal.reporting.report;

import com.thitsaworks.operation_portal.component.infra.mysql.hub.HubDataSourceConfiguration;
import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestSettings {

    @Bean
    @Qualifier(PersistenceQualifiers.Hub.READ_SETTINGS)
    public HubDataSourceConfiguration.Settings reportingDataSourceConfigurationReadDbSettings() {

        return new HubDataSourceConfiguration.Settings("jdbc:mysql://example.com:1234/central_ledger",
                                                       "example-user",
                                                       "example-password",
                                                       1,
                                                       5);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Hub.WRITE_SETTINGS)
    public HubDataSourceConfiguration.Settings reportingDataSourceConfigurationWriteDbSettings() {

        return new HubDataSourceConfiguration.Settings("jdbc:mysql://example.com:1234/central_ledger",
                                                       "example-user",
                                                       "example-password",
                                                       10,
                                                       20);
    }

}
