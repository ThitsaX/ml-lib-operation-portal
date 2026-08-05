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
package com.thitsaworks.operation_portal.api.operation.portal;

import com.thitsaworks.operation_portal.component.infra.mongo.ReportingMongoConfiguration;
import com.thitsaworks.operation_portal.component.infra.mysql.core.CoreDataSourceConfiguration;
import com.thitsaworks.operation_portal.component.infra.mysql.hub.HubDataSourceConfiguration;
import com.thitsaworks.operation_portal.component.infra.redis.RedisConfiguration;
import com.thitsaworks.operation_portal.component.infra.vault.Vault;
import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import com.thitsaworks.operation_portal.component.misc.storage.S3FileStorage;
import com.thitsaworks.operation_portal.core.revenue_config.engine.RevenueEngine;
import com.thitsaworks.operation_portal.core.email.EmailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;

public class VaultBasedApplicationSettings {

    @Bean
    public RedisConfiguration.Settings redisConfigurationSettings(Vault vault) {

        return vault.get(RedisConfiguration.REDIS_SETTINGS_PATH, RedisConfiguration.Settings.class);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Core.READ_SETTINGS)
    public CoreDataSourceConfiguration.Settings coreDataSourceConfigurationReadDbSettings(Vault vault) {

        return vault.get(
            CoreDataSourceConfiguration.READ_DB_SETTINGS_PATH,
            CoreDataSourceConfiguration.Settings.class);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Core.WRITE_SETTINGS)
    public CoreDataSourceConfiguration.Settings coreDataSourceConfigurationWriteDbSettings(Vault vault) {

        return vault.get(
            CoreDataSourceConfiguration.WRITE_DB_SETTINGS_PATH,
            CoreDataSourceConfiguration.Settings.class);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Hub.READ_SETTINGS)
    public HubDataSourceConfiguration.Settings reportingDataSourceConfigurationReadDbSettings(Vault vault) {

        return vault.get(
            HubDataSourceConfiguration.READ_DB_SETTINGS_PATH,
            HubDataSourceConfiguration.Settings.class);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Hub.WRITE_SETTINGS)
    public HubDataSourceConfiguration.Settings reportingDataSourceConfigurationWriteDbSettings(Vault vault) {

        return vault.get(
            HubDataSourceConfiguration.WRITE_DB_SETTINGS_PATH,
            HubDataSourceConfiguration.Settings.class);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Hub.MONGO_READ_SETTINGS)
    public ReportingMongoConfiguration.Settings reportingMongConfigurationReadDbSettings(Vault vault) {

        return vault.get(
            ReportingMongoConfiguration.READ_SETTINGS_PATH,
            ReportingMongoConfiguration.Settings.class);
    }

    @Bean
    @Qualifier(PersistenceQualifiers.Hub.MONGO_WRITE_SETTINGS)
    public ReportingMongoConfiguration.Settings reportingMongoConfigurationWriteDbSettings(Vault vault) {

        return vault.get(
            ReportingMongoConfiguration.WRITE_SETTINGS_PATH,
            ReportingMongoConfiguration.Settings.class);
    }

    @Bean
    public S3FileStorage.Settings s3Settings(Vault vault) {

        return vault.get(S3FileStorage.S3_SETTINGS_PATH, S3FileStorage.Settings.class);
    }

    @Bean
    public RevenueEngine.Settings revenueEngineSettings(Vault vault) {

        RevenueEngineVaultSettings settings =
            vault.get(RevenueEngine.SETTINGS_PATH, RevenueEngineVaultSettings.class);

        return new RevenueEngine.Settings(
            this.jobSchedule(settings.runStatusSchedule()),
            this.jobSchedule(settings.archiveSchedule()));
    }

    private RevenueEngine.JobSchedule jobSchedule(RevenueEngineVaultJobSchedule schedule) {

        return new RevenueEngine.JobSchedule(
            RevenueEngine.ScheduleMode.valueOf(schedule.mode().toUpperCase(Locale.ROOT)),
            schedule.delay(),
            schedule.period(),
            ZoneId.of(schedule.zoneId()),
            LocalTime.parse(schedule.time()));
    }

    public record RevenueEngineVaultSettings(RevenueEngineVaultJobSchedule runStatusSchedule,
                                             RevenueEngineVaultJobSchedule archiveSchedule) { }

    public record RevenueEngineVaultJobSchedule(String mode,
                                                long delay,
                                                long period,
                                                String zoneId,
                                                String time) { }

    public EmailConfiguration.EmailSettings emailSettings(Vault vault) {

        return vault.get(EmailConfiguration.EMAIL_SETTINGS_PATH, EmailConfiguration.EmailSettings.class);
    }

}
