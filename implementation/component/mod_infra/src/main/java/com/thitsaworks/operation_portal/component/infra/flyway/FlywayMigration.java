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

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlywayMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayMigration.class);

    public static void migrate(Settings settings) {

        Flyway.configure()
              .dataSource(settings.url(), settings.username(),
                          settings.password())
              .locations(settings.locations())
              .baselineOnMigrate(true)
              .load()
              .migrate();

    }

    public record Settings(String url, String username, String password, String... locations) { }

}
