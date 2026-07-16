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
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tbl_ndc_alert_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NdcAlertEvent {

    @EmbeddedId
    private NdcAlertEventId ndcAlertEventId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "participant_ndc_id", nullable = false))
    private ParticipantNDCId participantNDCId;

    @Column(name = "participant_name", nullable = false)
    private String participantName;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "breach_cycle_no", nullable = false)
    private long breachCycleNo;

    @Column(name = "previous_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcThresholdStateType previousState;

    @Column(name = "current_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcThresholdStateType currentState;

    @Column(name = "threshold_percent", nullable = false, precision = 7, scale = 4)
    private BigDecimal thresholdPercent;

    @Column(name = "current_balance", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentBalance;

    @Column(name = "current_ndc_used", nullable = false, precision = 7, scale = 4)
    private BigDecimal currentNdcUsed;

    @Column(name = "event_message")
    private String eventMessage;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    public NdcAlertEvent(ParticipantNDCId participantNDCId,
                         String participantName,
                         String currency,
                         long breachCycleNo,
                         BigDecimal thresholdPercent,
                         BigDecimal currentBalance,
                         BigDecimal currentNdcUsed,
                         String eventMessage,
                         LocalDateTime eventTime,
                         String createdBy) {

        if (breachCycleNo <= 0) {
            throw new IllegalArgumentException("breachCycleNo must be greater than zero");
        }

        this.ndcAlertEventId = new NdcAlertEventId(UUID.randomUUID());
        this.participantNDCId = Objects.requireNonNull(participantNDCId, "participantNDCId is required");
        this.participantName = Objects.requireNonNull(participantName, "participantName is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.breachCycleNo = breachCycleNo;
        this.previousState = NdcThresholdStateType.SAFE;
        this.currentState = NdcThresholdStateType.BREACHED;
        this.thresholdPercent = Objects.requireNonNull(thresholdPercent, "thresholdPercent is required");
        this.currentBalance = Objects.requireNonNull(currentBalance, "currentBalance is required");
        this.currentNdcUsed = Objects.requireNonNull(currentNdcUsed, "currentNdcUsed is required");
        this.eventMessage = eventMessage;
        this.eventTime = Objects.requireNonNull(eventTime, "eventTime is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
    }

    @PrePersist
    private void onCreate() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    @PreUpdate
    private void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }

    public NdcAlertEventId getId() {

        return this.ndcAlertEventId;
    }
}
