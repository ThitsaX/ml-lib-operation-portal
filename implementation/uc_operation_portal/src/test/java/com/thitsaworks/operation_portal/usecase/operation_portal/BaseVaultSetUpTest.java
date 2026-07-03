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
package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.infra.flyway.DatabaseMigration;
import com.thitsaworks.operation_portal.component.infra.mysql.core.CoreDataSourceConfiguration;
import com.thitsaworks.operation_portal.component.infra.vault.VaultConfiguration;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseVaultSetUpTest {

    @BeforeAll
    public static void beforeAll() {

        System.setProperty("VAULT_ADDR", "http://127.0.0.1:8200");
        System.setProperty("VAULT_TOKEN", "example-token");
        System.setProperty("ENGINE_PATH", "operation_portal");

        DatabaseMigration.migrate(CoreDataSourceConfiguration.FLYWAY_MIGRATION,
                new VaultConfiguration.Settings(System.getProperty("VAULT_ADDR"), System.getProperty("VAULT_TOKEN"), System.getProperty("ENGINE_PATH")));
    }

}
