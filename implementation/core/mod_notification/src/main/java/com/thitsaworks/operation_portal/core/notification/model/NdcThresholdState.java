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

import com.thitsaworks.operation_portal.component.common.identifier.NdcThresholdStateId;
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
@Table(name = "tbl_ndc_threshold_state")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NdcThresholdState extends JpaEntity<NdcThresholdStateId> {

    @EmbeddedId
    private NdcThresholdStateId ndcThresholdStateId;

    @Column(name = "participant_name", nullable = false, length = 100)
    private String participantName;

    @Column(name = "currency", nullable = false, length = 100)
    private String currency;

    @Column(name = "current_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcThresholdStateType currentState;

    @Column(name = "breach_cycle_no", nullable = false)
    private long breachCycleNo;

    @Column(name = "last_evaluated_balance", precision = 18, scale = 4)
    private BigDecimal lastEvaluatedBalance;

    @Column(name = "last_evaluated_ndc_used", precision = 7, scale = 4)
    private BigDecimal lastEvaluatedNdcUsed;

    @Column(name = "last_breached_at")
    @Convert(converter = JpaInstantConverter.class)
    private Instant lastBreachedAt;

    @Column(name = "last_recovered_at")
    @Convert(converter = JpaInstantConverter.class)
    private Instant lastRecoveredAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public NdcThresholdState(String participantName,
                             String currency,
                             String createdBy) {

        this.ndcThresholdStateId = new NdcThresholdStateId(Snowflake.get().nextId());
        this.participantName = Objects.requireNonNull(participantName, "participantName is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.currentState = NdcThresholdStateType.SAFE;
        this.breachCycleNo = 0L;
    }

    public void recordEvaluation(BigDecimal currentPosition,
                                 BigDecimal ndcUsedPercent,
                                 String updatedBy) {

        this.lastEvaluatedBalance = currentPosition;
        this.lastEvaluatedNdcUsed = ndcUsedPercent;
        this.updatedBy = updatedBy;
    }

    public boolean breach(Instant breachedAt, String updatedBy) {

        if (this.currentState == NdcThresholdStateType.BREACHED) {
            return false;
        }

        this.currentState = NdcThresholdStateType.BREACHED;
        this.breachCycleNo++;
        this.lastBreachedAt = Objects.requireNonNull(breachedAt, "breachedAt is required");
        this.updatedBy = updatedBy;
        return true;
    }

    public boolean recover(Instant recoveredAt, String updatedBy) {

        if (this.currentState == NdcThresholdStateType.SAFE) {
            return false;
        }

        this.currentState = NdcThresholdStateType.SAFE;
        this.lastRecoveredAt = Objects.requireNonNull(recoveredAt, "recoveredAt is required");
        this.updatedBy = updatedBy;
        return true;
    }

    @Override
    public NdcThresholdStateId getId() {

        return this.ndcThresholdStateId;
    }
}
