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
package com.thitsaworks.operation_portal.component.infra.flyway;


import com.thitsaworks.operation_portal.component.infra.vault.Vault;
import com.thitsaworks.operation_portal.component.infra.vault.VaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMigration {

    private static  final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigration.class);

    public static void migrate(String vaultPath, VaultConfiguration.Settings vaultSettings) {

        LOGGER.info("vaultSettings : <{}>", vaultSettings);
        var vault = new Vault(vaultSettings.address(), vaultSettings.token(), vaultSettings.enginePath());

        LOGGER.info("Done loading Vault with vaultSettings : <{}>", vaultSettings);
        DatabaseMigration.migrate(vault.get(vaultPath, FlywayMigration.Settings.class));

    }

    public static void migrate(FlywayMigration.Settings settings) {

        LOGGER.info("Migrating database...");
        FlywayMigration.migrate(settings);
        LOGGER.info("Done migrating database...");
    }

}
