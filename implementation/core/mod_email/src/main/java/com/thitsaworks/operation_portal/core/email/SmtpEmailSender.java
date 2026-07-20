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

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SmtpEmailSender implements EmailSender {

    private static final Logger LOG = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final EmailConfiguration.EmailSettings settings;

    public SmtpEmailSender(EmailConfiguration.EmailSettings settings) {

        this.settings = settings;
    }

    @Override
    public void send(String receiverEmail, String subject, String content) {

        this.validate(receiverEmail);

        try {

            LOG.info(
                "Sending email via SMTP: receiverEmail=[{}], subject=[{}], host=[{}], port=[{}]",
                receiverEmail, subject, this.settings.host(), this.settings.smtpPort());

            MimeMessage message = new MimeMessage(this.session());
            message.setFrom(new InternetAddress(this.settings.senderEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            LOG.info("Email sent via SMTP: receiverEmail=[{}], subject=[{}]", receiverEmail, subject);

        } catch (MessagingException e) {

            LOG.error("Failed to send email via SMTP: receiverEmail=[{}], subject=[{}]", receiverEmail, subject, e);

            throw new IllegalStateException("Failed to send email.", e);
        }
    }

    private Session session() {

        Properties properties = new Properties();
        properties.put("mail.smtp.host", this.required(this.settings.host(), "host"));
        properties.put("mail.smtp.port", String.valueOf(this.settings.smtpPort()));
        properties.put("mail.smtp.auth", String.valueOf(this.settings.smtpAuth()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(this.settings.smtpStartTlsEnable()));
        properties.put("mail.smtp.ssl.enable", String.valueOf(this.settings.smtpSslEnable()));

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(
                    SmtpEmailSender.this.required(
                        SmtpEmailSender.this.settings.senderEmail(), "senderEmail"),
                    SmtpEmailSender.this.required(
                        SmtpEmailSender.this.settings.password(), "password"));
            }
        });
    }

    private void validate(String receiverEmail) {

        this.required(receiverEmail, "receiverEmail");
        this.required(this.settings.senderEmail(), "senderEmail");
        this.required(this.settings.password(), "password");
    }

    private String required(String value, String name) {

        if (value == null || value.isBlank()) {

            throw new IllegalStateException("Email setting [" + name + "] is required.");
        }

        return value;
    }

}
