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
package com.thitsaworks.operation_portal.component.infra.mysql.core;

import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Import(value = {CoreDataSourceConfiguration.class})
public class CoreJdbcPersistenceConfiguration {

    @Primary
    @Bean(name = PersistenceQualifiers.Core.WRITE_JDBC_TEMPLATE)
    public JdbcTemplate writeJdbcTemplate(@Qualifier(PersistenceQualifiers.Core.WRITE_DATA_SOURCE)
                                          DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }

    @Bean(name = PersistenceQualifiers.Core.READ_JDBC_TEMPLATE)
    public JdbcTemplate readJdbcTemplate(
        @Qualifier(PersistenceQualifiers.Core.READ_DATA_SOURCE) DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }

}
