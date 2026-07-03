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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

public class CoreDataSourceConfiguration {

    public  static final String FLYWAY_MIGRATION ="mysql/portal_data/flyway/settings";

    public static final String WRITE_DB_SETTINGS_PATH = "mysql/portal_data/write_db/settings";

    public static final String READ_DB_SETTINGS_PATH = "mysql/portal_data/read_db/settings";

    @Bean(name = PersistenceQualifiers.Core.READ_DATA_SOURCE)
    @Qualifier(PersistenceQualifiers.Core.READ_DATA_SOURCE)
    public DataSource readDataSource(@Qualifier(PersistenceQualifiers.Core.READ_SETTINGS) Settings settings) {

        var config = new HikariConfig();

        config.setPoolName("Hikari-Core-Read-Pool");
        config.setJdbcUrl(settings.url());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        config.setKeepaliveTime(30000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);


        config.setMaximumPoolSize(settings.maxPoolSize());
        config.setAutoCommit(false);

        return new HikariDataSource(config);

    }

    @Bean(name = PersistenceQualifiers.Core.WRITE_DATA_SOURCE)
    @Qualifier(PersistenceQualifiers.Core.WRITE_DATA_SOURCE)
    public DataSource writeDataSource(
        @Qualifier(PersistenceQualifiers.Core.WRITE_SETTINGS) Settings settings) {

        var config = new HikariConfig();

        config.setPoolName("Hikari-Core-Write-Pool");
        config.setJdbcUrl(settings.url());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);

        config.setMaximumPoolSize(settings.maxPoolSize());
        config.setAutoCommit(false);

        return new HikariDataSource(config);

    }

    public record Settings(String url, String username, String password, int minPoolSize, int maxPoolSize) { }

}
