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
package com.thitsaworks.operation_portal.component.infra.vault;

import org.springframework.context.annotation.Bean;

public class VaultConfiguration {

    @Bean
    public Vault vault(Settings settings) {

        return new Vault(settings.address, settings.token, settings.enginePath);
    }

    public record Settings(String address, String token, String enginePath) {

        public static Settings withProperty() {

            var address = System.getProperty("VAULT_ADDR");
            var token = System.getProperty("VAULT_TOKEN");
            var enginePath = System.getProperty("ENGINE_PATH");

            return new Settings(address, token, enginePath);
        }

        public static Settings withEnv() {

            var address = System.getenv("VAULT_ADDR");
            var token = System.getenv("VAULT_TOKEN");
            var enginePath = System.getenv("ENGINE_PATH");

            return new Settings(address, token, enginePath);
        }

        public static Settings withPropertyOrEnv() {

            var address = System.getenv("VAULT_ADDR") == null ? System.getProperty("VAULT_ADDR") : System.getenv("VAULT_ADDR");
            var token = System.getenv("VAULT_TOKEN") == null ? System.getProperty("VAULT_TOKEN") : System.getenv("VAULT_TOKEN");
            var enginePath = System.getenv("ENGINE_PATH") == null ? System.getProperty("ENGINE_PATH") : System.getenv("ENGINE_PATH");

            return new Settings(address, token, enginePath);
        }

    }

}