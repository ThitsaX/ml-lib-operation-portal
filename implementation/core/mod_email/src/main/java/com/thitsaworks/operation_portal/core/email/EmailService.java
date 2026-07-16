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

import java.math.BigDecimal;

public class EmailService {

    private static final String SUBJECT = "NDC Usage Alert - Action Required";

    private static final String MESSAGE =
        "Dear User,\n\n" +
            "The NDC usage threshold has been reached.\n\n" +
            "DFSP: <<dfsp_name>>\n" +
            "Currency: <<currency>>\n" +
            "Configured Threshold: <<threshold_percentage>>%\n" +
            "Current NDC Usage: <<ndc_used_percentage>>%\n\n" +
            "Please deposit additional funds to prevent transaction blockage.\n\n" +
            "This is an automated notification. Please do not reply to this email.\n\n" +
            "Regards,\n" +
            "Operations Team";

    private final EmailSender emailSender;

    private final UserQuery userQuery;

    public EmailService(EmailSender emailSender, UserQuery userQuery) {

        this.emailSender = emailSender;
        this.userQuery = userQuery;
    }

    public void sendNdcUsageAlertToUser(UserId receiverUserId,
                                        String dfspName,
                                        String currency,
                                        BigDecimal ndcUsedPercentage) throws ParticipantException {

        UserData user = this.userQuery.get(receiverUserId);

        if (user.email() == null) {
            return;
        }

        this.sendNdcUsageAlertToEmail(
            user.email().getValue(),
            dfspName,
            currency,
            ndcUsedPercentage,
            BigDecimal.valueOf(80)
        );
    }

    public void sendNdcUsageAlertToEmail(String receiverEmail,
                                         String dfspName,
                                         String currency,
                                         BigDecimal ndcUsedPercentage,
                                         BigDecimal thresholdPercentage) {

        if (receiverEmail == null || receiverEmail.isBlank()) {
            throw new IllegalArgumentException("receiverEmail is required");
        }

        if (dfspName == null || dfspName.isBlank()) {
            throw new IllegalArgumentException("dfspName is required");
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }

        if (ndcUsedPercentage == null || thresholdPercentage == null) {
            throw new IllegalArgumentException("NDC percentages are required");
        }

        String content = MESSAGE
            .replace("<<dfsp_name>>", dfspName)
            .replace("<<currency>>", currency)
            .replace("<<threshold_percentage>>", thresholdPercentage.toPlainString())
            .replace("<<ndc_used_percentage>>", ndcUsedPercentage.toPlainString());

        this.emailSender.send(receiverEmail, SUBJECT, content);
    }

}
