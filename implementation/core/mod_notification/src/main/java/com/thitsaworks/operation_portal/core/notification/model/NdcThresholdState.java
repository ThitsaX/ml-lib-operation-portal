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
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tbl_ndc_threshold_state")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NdcThresholdState extends JpaEntity<NdcThresholdStateId> {

    @EmbeddedId
    private NdcThresholdStateId ndcThresholdStateId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "participant_ndc_id", nullable = false))
    private ParticipantNDCId participantNDCId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "threshold_configuration_id", nullable = false))
    private ThresholdConfigurationId thresholdConfigurationId;

    @Column(name = "current_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcThresholdStateType currentState;

    @Column(name = "breach_cycle_no", nullable = false)
    private long breachCycleNo;

    @Column(name = "last_current_position", precision = 18, scale = 4)
    private BigDecimal lastCurrentPosition;

    @Column(name = "last_ndc_amount", precision = 18, scale = 4)
    private BigDecimal lastNdcAmount;

    @Column(name = "last_ndc_used_percent", precision = 7, scale = 4)
    private BigDecimal lastNdcUsedPercent;

    @Column(name = "last_breached_at")
    private LocalDateTime lastBreachedAt;

    @Column(name = "last_recovered_at")
    private LocalDateTime lastRecoveredAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public NdcThresholdState(ParticipantNDCId participantNDCId,
                             ThresholdConfigurationId thresholdConfigurationId,
                             String createdBy) {

        this.ndcThresholdStateId = new NdcThresholdStateId(Snowflake.get().nextId());
        this.participantNDCId = Objects.requireNonNull(participantNDCId, "participantNDCId is required");
        this.thresholdConfigurationId = Objects.requireNonNull(thresholdConfigurationId,
                                                               "thresholdConfigurationId is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.currentState = NdcThresholdStateType.SAFE;
        this.breachCycleNo = 0L;
    }

    public void recordEvaluation(BigDecimal currentPosition,
                                 BigDecimal ndcAmount,
                                 BigDecimal ndcUsedPercent,
                                 String updatedBy) {

        this.lastCurrentPosition = currentPosition;
        this.lastNdcAmount = ndcAmount;
        this.lastNdcUsedPercent = ndcUsedPercent;
        this.updatedBy = updatedBy;
    }

    public boolean breach(LocalDateTime breachedAt, String updatedBy) {

        if (this.currentState == NdcThresholdStateType.BREACHED) {
            return false;
        }

        this.currentState = NdcThresholdStateType.BREACHED;
        this.breachCycleNo++;
        this.lastBreachedAt = Objects.requireNonNull(breachedAt, "breachedAt is required");
        this.updatedBy = updatedBy;
        return true;
    }

    public boolean recover(LocalDateTime recoveredAt, String updatedBy) {

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
