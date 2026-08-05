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
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaInstantConverter;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tbl_ndc_alert_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NdcAlertEvent extends JpaEntity<NdcAlertEventId> {

    @EmbeddedId
    private NdcAlertEventId ndcAlertEventId;

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

    @Column(name = "threshold_percent", nullable = false, precision = 18, scale = 4)
    private BigDecimal thresholdPercent;

    @Column(name = "current_position", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentPosition;

    @Column(name = "ndc_limit", nullable = false, precision = 18, scale = 4)
    private BigDecimal ndcLimit;

    @Column(name = "current_ndc_used", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentNdcUsed;

    @Column(name = "event_message")
    private String eventMessage;

    @Column(name = "event_time", nullable = false)
    @Convert(converter = JpaInstantConverter.class)
    private Instant eventTime;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public NdcAlertEvent(String participantName,
                         String currency,
                         long breachCycleNo,
                         BigDecimal thresholdPercent,
                         BigDecimal currentPosition,
                         BigDecimal ndcLimit,
                         BigDecimal currentNdcUsed,
                         String eventMessage,
                         Instant eventTime,
                         String createdBy) {

        if (breachCycleNo <= 0) {
            throw new IllegalArgumentException("breachCycleNo must be greater than zero");
        }

        this.ndcAlertEventId = new NdcAlertEventId(Snowflake.get().nextId());
        this.participantName = Objects.requireNonNull(participantName, "participantName is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.breachCycleNo = breachCycleNo;
        this.previousState = NdcThresholdStateType.SAFE;
        this.currentState = NdcThresholdStateType.BREACHED;
        this.thresholdPercent = Objects.requireNonNull(thresholdPercent, "thresholdPercent is required");
        this.currentPosition = Objects.requireNonNull(currentPosition, "currentPosition is required");
        this.ndcLimit = Objects.requireNonNull(ndcLimit, "ndcLimit is required");
        this.currentNdcUsed = Objects.requireNonNull(currentNdcUsed, "currentNdcUsed is required");
        this.eventMessage = eventMessage;
        this.eventTime = Objects.requireNonNull(eventTime, "eventTime is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
    }

    @Override
    public NdcAlertEventId getId() {

        return this.ndcAlertEventId;
    }
}
