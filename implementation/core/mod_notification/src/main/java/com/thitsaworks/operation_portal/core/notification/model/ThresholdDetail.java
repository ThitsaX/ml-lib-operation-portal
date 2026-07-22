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

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tbl_threshold_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThresholdDetail {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @EmbeddedId
    private ThresholdDetailId thresholdDetailId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "threshold_id", nullable = false))
    private ThresholdConfigurationId thresholdConfigurationId;

    @Column(name = "participant_currency_id", nullable = false)
    private Long participantCurrencyId;

    @Column(name = "currency", nullable = false, length = 100)
    private String currency;

    @Column(name = "visual_config", nullable = false, precision = 7, scale = 4)
    private BigDecimal visualConfig;

    @Column(name = "ndc_config", nullable = false, precision = 7, scale = 4)
    private BigDecimal ndcConfig;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public ThresholdDetail(ThresholdConfigurationId thresholdConfigurationId,
                           Long participantCurrencyId,
                           String currency,
                           BigDecimal visualConfig,
                           BigDecimal ndcConfig,
                           String createdBy) {

        validate(participantCurrencyId, currency, visualConfig, ndcConfig);

        this.thresholdDetailId = new ThresholdDetailId(Snowflake.get().nextId());
        this.thresholdConfigurationId = Objects.requireNonNull(
            thresholdConfigurationId, "thresholdConfigurationId is required");
        this.participantCurrencyId = participantCurrencyId;
        this.currency = currency;
        this.visualConfig = visualConfig;
        this.ndcConfig = ndcConfig;
        this.status = true;
        this.createdBy = requireText(createdBy, "createdBy");
    }

    public void update(Long participantCurrencyId,
                       String currency,
                       BigDecimal visualConfig,
                       BigDecimal ndcConfig,
                       boolean status,
                       String updatedBy) {

        validate(participantCurrencyId, currency, visualConfig, ndcConfig);

        this.participantCurrencyId = participantCurrencyId;
        this.currency = currency;
        this.visualConfig = visualConfig;
        this.ndcConfig = ndcConfig;
        this.status = status;
        this.updatedBy = requireText(updatedBy, "updatedBy");
    }

    public void deactivate(String updatedBy) {

        this.status = false;
        this.updatedBy = requireText(updatedBy, "updatedBy");
    }

    private static void validate(Long participantCurrencyId,
                                 String currency,
                                 BigDecimal visualConfig,
                                 BigDecimal ndcConfig) {

        if (participantCurrencyId == null || participantCurrencyId <= 0) {
            throw new IllegalArgumentException("participantCurrencyId must be greater than zero");
        }

        requireText(currency, "currency");
        validatePercentage(visualConfig, "visualConfig");
        validatePercentage(ndcConfig, "ndcConfig");

        if (visualConfig.compareTo(ndcConfig) > 0) {
            throw new IllegalArgumentException("visualConfig cannot be greater than ndcConfig");
        }
    }

    private static void validatePercentage(BigDecimal value, String name) {

        Objects.requireNonNull(value, name + " is required");

        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException(name + " must be between 0 and 100");
        }
    }

    private static String requireText(String value, String name) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }

        return value;
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

    public ThresholdDetailId getId() {

        return this.thresholdDetailId;
    }
}
