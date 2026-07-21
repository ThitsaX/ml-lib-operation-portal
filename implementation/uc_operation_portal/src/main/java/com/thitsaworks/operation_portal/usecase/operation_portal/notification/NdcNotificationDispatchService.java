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
package com.thitsaworks.operation_portal.usecase.operation_portal.notification;

import com.thitsaworks.operation_portal.component.common.identifier.NdcNotificationDispatchLogId;
import com.thitsaworks.operation_portal.component.common.type.NdcRecipientType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.email.EmailService;
import com.thitsaworks.operation_portal.core.notification.model.NdcAlertEvent;
import com.thitsaworks.operation_portal.core.notification.model.NdcNotificationDispatchLog;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcAlertEventRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcNotificationDispatchLogRepository;
import com.thitsaworks.operation_portal.core.participant.data.UserData;
import com.thitsaworks.operation_portal.core.participant.query.UserQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NdcNotificationDispatchService {

    private static final Logger LOG = LoggerFactory.getLogger(NdcNotificationDispatchService.class);

    private static final String SYSTEM_USER = "system";

    private static final int MAXIMUM_ATTEMPTS = 3;

    private final NdcAlertEventRepository alertEventRepository;

    private final NdcNotificationDispatchLogRepository dispatchLogRepository;

    private final UserQuery userQuery;

    private final EmailService emailService;

    @CoreWriteTransactional
    public List<NdcNotificationDispatchLogId> createDispatchLogs(
        NdcAlertEvent alertEvent) {

        Map<Long, UserData> recipients = resolveRecipients(alertEvent);

        return recipients.values()
                         .stream()
                         .map(user -> createDispatchLog(alertEvent, user))
                         .toList();

    }

    @CoreWriteTransactional
    public DeliveryResult deliver(NdcNotificationDispatchLogId dispatchLogId) {

        LOG.info("NDC email delivery.");

        NdcNotificationDispatchLog dispatchLog =
            dispatchLogRepository.findByIdForUpdate(dispatchLogId)
                                  .orElseThrow(() -> new IllegalStateException(
                                      "Dispatch log not found: " + dispatchLogId));

        if (!dispatchLog.canAttempt(MAXIMUM_ATTEMPTS)) {
            LOG.warn("Skipping NDC email delivery because no attempts remain: dispatchLogId={}, "
                         + "alertEventId={}, recipientUserId={}, status={}, attempt={}",
                     dispatchLog.getNdcNotificationDispatchLogId(), dispatchLog.getAlertEventId(),
                     dispatchLog.getRecipientUserId(), dispatchLog.getDeliveryStatus(),
                     dispatchLog.getAttemptNo());
            return new DeliveryResult(false, false, true);
        }

        dispatchLog.startAttempt(LocalDateTime.now(), SYSTEM_USER);

        LOG.info("NDC email delivery attempt: dispatchLogId={}, alertEventId={}, "
                     + "recipientUserId={}, recipientType={}, attempt={}",
                 dispatchLog.getNdcNotificationDispatchLogId(), dispatchLog.getAlertEventId(),
                 dispatchLog.getRecipientUserId(), dispatchLog.getRecipientType(),
                 dispatchLog.getAttemptNo());

        try {
            NdcAlertEvent alertEvent =
                alertEventRepository.findById(dispatchLog.getAlertEventId())
                                    .orElseThrow(() -> new IllegalStateException(
                                        "Alert event not found: " + dispatchLog.getAlertEventId()));

            AlertMessage alertMessage = parseAlertMessage(alertEvent.getEventMessage());

            emailService.sendNdcUsageAlertToEmail(
                dispatchLog.getRecipientEmail(),
                alertMessage.subject(),
                alertMessage.content());

            dispatchLog.markSent(LocalDateTime.now(), SYSTEM_USER);
            dispatchLogRepository.saveAndFlush(dispatchLog);

            LOG.info("NDC email delivery succeeded: dispatchLogId={}, alertEventId={}, "
                         + "recipientUserId={}, attempt={}",
                     dispatchLog.getNdcNotificationDispatchLogId(), dispatchLog.getAlertEventId(),
                     dispatchLog.getRecipientUserId(), dispatchLog.getAttemptNo());

            return new DeliveryResult(true, false, false);

        } catch (RuntimeException exception) {
            dispatchLog.markFailed(exception.getMessage(), SYSTEM_USER);
            dispatchLogRepository.saveAndFlush(dispatchLog);

            LOG.error("NDC email delivery failed: dispatchLogId={}, alertEventId={}, "
                          + "recipientUserId={}, attempt={}, error={}",
                      dispatchLog.getNdcNotificationDispatchLogId(), dispatchLog.getAlertEventId(),
                      dispatchLog.getRecipientUserId(), dispatchLog.getAttemptNo(),
                      exception.getMessage(), exception);

            return new DeliveryResult(false, true, false);
        }
    }

    private NdcNotificationDispatchLogId createDispatchLog(
        NdcAlertEvent alertEvent,
        UserData user) {

        String recipientUserId = user.userId().getId().toString();

        var existing = dispatchLogRepository.findByAlertEventIdAndRecipientUserId(
            alertEvent.getNdcAlertEventId(),
            recipientUserId);

        if (existing.isPresent()) {
            return existing.get().getNdcNotificationDispatchLogId();
        }

        if (user.email() == null) {
            throw new IllegalStateException(
                "Recipient email is missing for user: " + recipientUserId);
        }

        NdcRecipientType recipientType = isHubUser(user)
            ? NdcRecipientType.HUB
            : NdcRecipientType.DFSP;

        NdcNotificationDispatchLog dispatchLog =
            new NdcNotificationDispatchLog(
                alertEvent.getNdcAlertEventId(),
                alertEvent.getParticipantNDCId(),
                recipientType,
                recipientUserId,
                user.name(),
                user.email().getValue(),
                SYSTEM_USER);

        LOG.info("Create Dispatch Logs.");

        return dispatchLogRepository.saveAndFlush(dispatchLog)
                                    .getNdcNotificationDispatchLogId();
    }

    private Map<Long, UserData> resolveRecipients(NdcAlertEvent alertEvent) {

        String breachedDfsp = alertEvent.getParticipantName();
        Map<Long, UserData> recipients = new LinkedHashMap<>();

        for (UserData user : userQuery.getUsers()) {

            if (user.email() == null || user.participantName() == null) {
                continue;
            }

            if (!user.allowNotification()) {
                continue;
            }

            String participantName = user.participantName().getValue();

            LOG.info("User participantName", user.participantName());

            boolean hubUser = participantName.toUpperCase(Locale.ROOT).contains("HUB");
            boolean breachedDfspUser = participantName.equals(breachedDfsp);

            if (hubUser || breachedDfspUser) {
                LOG.info("TRUE");
                recipients.put(user.userId().getId(), user);
            }
        }

        long hubRecipients = recipients.values().stream().filter(this::isHubUser).count();
        long dfspRecipients = recipients.size() - hubRecipients;

        if (recipients.isEmpty()) {
            LOG.warn("No eligible NDC notification recipients found: alertEventId={}, breachedDfsp={}",
                     alertEvent.getNdcAlertEventId(), breachedDfsp);
        } else {
            LOG.info("NDC notification recipients resolved: alertEventId={}, breachedDfsp={}, "
                         + "hubRecipients={}, dfspRecipients={}, totalRecipients={}",
                     alertEvent.getNdcAlertEventId(), breachedDfsp, hubRecipients,
                     dfspRecipients, recipients.size());
        }

        return recipients;
    }

    private boolean isHubUser(UserData user) {

        return user.participantName().getValue()
                  .toUpperCase(Locale.ROOT)
                  .contains("HUB");
    }

    private AlertMessage parseAlertMessage(String eventMessage) {

        if (eventMessage == null || eventMessage.isBlank()) {
            throw new IllegalStateException("Alert event message is required");
        }

        String normalizedMessage = eventMessage.stripLeading();
        int subjectEndIndex = normalizedMessage.indexOf('\n');

        if (subjectEndIndex < 0) {
            throw new IllegalStateException("Alert event message content is required");
        }

        String subject = normalizedMessage.substring(0, subjectEndIndex).strip();
        String content = normalizedMessage.substring(subjectEndIndex + 1).stripLeading();

        if (subject.endsWith(",")) {
            subject = subject.substring(0, subject.length() - 1).strip();
        }

        if (subject.isBlank() || content.isBlank()) {
            throw new IllegalStateException("Alert event message subject and content are required");
        }

        return new AlertMessage(subject, content);
    }

    public record DeliveryResult(boolean sent,
                                 boolean failed,
                                 boolean skipped) {
    }

    private record AlertMessage(String subject,
                                String content) {
    }
}
