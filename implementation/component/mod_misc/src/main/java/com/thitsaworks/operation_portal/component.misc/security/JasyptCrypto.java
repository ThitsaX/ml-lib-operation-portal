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
package com.thitsaworks.operation_portal.component.misc.security;

import lombok.Getter;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;

@Getter
public class JasyptCrypto {

    private final StandardPBEStringEncryptor encryptor;

    public JasyptCrypto(String envName) {

        EnvironmentPBEConfig config = new EnvironmentPBEConfig();

        config.setAlgorithm("PBEWithMD5AndDES");

        String password = System.getenv(envName);

        if (password == null) {

            password = System.getProperty(envName);

        }

        config.setPassword(password);

        this.encryptor = new StandardPBEStringEncryptor();
        this.encryptor.setConfig(config);

    }

    public String decrypt(String encrypted) {

        return PropertyValueEncryptionUtils.decrypt(encrypted, this.encryptor);

    }

    public String encrypt(String plain) {

        return PropertyValueEncryptionUtils.encrypt(plain, this.encryptor);

    }

}
