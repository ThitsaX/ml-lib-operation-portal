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

import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.core.participant.data.UserData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.query.UserQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final EmailSender emailSender;

    private final UserQuery userQuery;

    public EmailService(EmailSender emailSender, UserQuery userQuery) {

        this.emailSender = emailSender;
        this.userQuery = userQuery;
    }

    public void sendNdcUsageAlertToEmail(String receiverEmail,
                                         String subject,
                                         String message) {


        if (receiverEmail == null || receiverEmail.isBlank()) {
            throw new IllegalArgumentException("receiverEmail is required");
        }

        LOG.info(
            "Sending NDC usage alert email: receiverEmail=[{}]", receiverEmail);

        this.emailSender.send(receiverEmail, subject, message);

        LOG.info(
            "NDC usage alert email sent: receiverEmail=[{}]", receiverEmail);
    }

}
