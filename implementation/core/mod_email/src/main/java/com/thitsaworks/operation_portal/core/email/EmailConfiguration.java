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
package com.thitsaworks.operation_portal.core.email;

import com.thitsaworks.operation_portal.core.participant.query.UserQuery;
import org.springframework.context.annotation.Bean;

public class EmailConfiguration {

    public static final String EMAIL_SETTINGS_PATH = "email/settings";

    @Bean
    public EmailSender emailSender(EmailSettings settings) {
        return new SmtpEmailSender(settings);
    }

    @Bean
    public EmailService emailService(EmailSender emailSender, UserQuery userQuery) {

        return new EmailService(emailSender, userQuery);
    }

    public record EmailSettings(String host,
                           Integer port,
                           String senderName,
                           String senderEmail,
                           String password,
                           Boolean auth,
                           Boolean startTlsEnable,
                           Boolean sslEnable) {

        private static final String DEFAULT_SENDER_NAME = "Operation Portal";

        public int smtpPort() {

            return this.port == null ? 587 : this.port;
        }

        public boolean smtpAuth() {

            return this.auth == null || this.auth;
        }

        public boolean smtpStartTlsEnable() {

            return this.startTlsEnable == null || this.startTlsEnable;
        }

        public boolean smtpSslEnable() {

            return this.sslEnable != null && this.sslEnable;
        }

        public String displaySenderName() {

            return this.senderName == null || this.senderName.isBlank()
                ? DEFAULT_SENDER_NAME
                : this.senderName;
        }

    }

}
