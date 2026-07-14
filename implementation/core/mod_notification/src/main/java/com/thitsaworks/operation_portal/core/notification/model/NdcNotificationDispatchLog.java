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
package com.thitsaworks.operation_portal.core.notification.model;

import com.thitsaworks.operation_portal.component.common.identifier.NdcAlertEventId;
import com.thitsaworks.operation_portal.component.common.identifier.NdcNotificationDispatchLogId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.component.common.type.NdcRecipientType;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tbl_ndc_notification_dispatch_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NdcNotificationDispatchLog extends JpaEntity<NdcNotificationDispatchLogId> {

    @EmbeddedId
    private NdcNotificationDispatchLogId ndcNotificationDispatchLogId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "alert_event_id", nullable = false))
    private NdcAlertEventId alertEventId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "participant_ndc_id", nullable = false))
    private ParticipantNDCId participantNDCId;

    @Column(name = "recipient_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcRecipientType recipientType;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "recipient_user_id", nullable = false))
    private UserId recipientUserId;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "delivery_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcDeliveryStatus deliveryStatus;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public NdcNotificationDispatchLog(NdcAlertEventId alertEventId,
                                      ParticipantNDCId participantNDCId,
                                      NdcRecipientType recipientType,
                                      UserId recipientUserId,
                                      String recipientName,
                                      String recipientEmail,
                                      String createdBy) {

        this.ndcNotificationDispatchLogId = new NdcNotificationDispatchLogId(Snowflake.get().nextId());
        this.alertEventId = Objects.requireNonNull(alertEventId, "alertEventId is required");
        this.participantNDCId = Objects.requireNonNull(participantNDCId, "participantNDCId is required");
        this.recipientType = Objects.requireNonNull(recipientType, "recipientType is required");
        this.recipientUserId = Objects.requireNonNull(recipientUserId, "recipientUserId is required");
        this.recipientName = recipientName;
        this.recipientEmail = Objects.requireNonNull(recipientEmail, "recipientEmail is required");
        this.deliveryStatus = NdcDeliveryStatus.PENDING;
        this.attemptNo = 0;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
    }

    public boolean canAttempt(int maximumAttempts) {

        return this.deliveryStatus != NdcDeliveryStatus.SENT && this.attemptNo < maximumAttempts;
    }

    public void startAttempt(LocalDateTime attemptTime, String updatedBy) {

        if (this.deliveryStatus == NdcDeliveryStatus.SENT) {
            throw new IllegalStateException("A successfully delivered notification cannot be resent");
        }

        this.attemptNo++;
        this.lastAttemptAt = Objects.requireNonNull(attemptTime, "attemptTime is required");
        this.deliveryStatus = this.attemptNo == 1 ? NdcDeliveryStatus.PENDING : NdcDeliveryStatus.RETRYING;
        this.errorMessage = null;
        this.updatedBy = updatedBy;
    }

    public void markSent(LocalDateTime sentAt, String updatedBy) {

        this.deliveryStatus = NdcDeliveryStatus.SENT;
        this.sentAt = Objects.requireNonNull(sentAt, "sentAt is required");
        this.errorMessage = null;
        this.updatedBy = updatedBy;
    }

    public void markFailed(String errorMessage, String updatedBy) {

        this.deliveryStatus = NdcDeliveryStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedBy = updatedBy;
    }

    @Override
    public NdcNotificationDispatchLogId getId() {

        return this.ndcNotificationDispatchLogId;
    }
}
