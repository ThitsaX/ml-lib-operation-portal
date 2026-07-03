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

import com.thitsaworks.operation_portal.component.infra.flyway.DatabaseMigration;
import com.thitsaworks.operation_portal.component.infra.mysql.core.CoreDataSourceConfiguration;
import com.thitsaworks.operation_portal.component.infra.vault.VaultConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Import(value = {OperationPortalApiConfiguration.class})
@EnableScheduling
@SpringBootApplication(
    exclude = {
        HibernateJpaAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        FlywayAutoConfiguration.class})
public class WebApiOperationPortalApplication {

    public static void main(String[] args) {

        VaultConfiguration.Settings vaultSettings = VaultConfiguration.Settings.withPropertyOrEnv();
        DatabaseMigration.migrate(CoreDataSourceConfiguration.FLYWAY_MIGRATION, vaultSettings);

        System.setProperty("management.endpoints.web.exposure.include", "health");
        System.setProperty("management.endpoint.health.show-details", "always");

        SpringApplication.run(WebApiOperationPortalApplication.class, args);
    }

    @Bean
    public VaultConfiguration.Settings vaultSettings() {

        return VaultConfiguration.Settings.withPropertyOrEnv();
    }

}
