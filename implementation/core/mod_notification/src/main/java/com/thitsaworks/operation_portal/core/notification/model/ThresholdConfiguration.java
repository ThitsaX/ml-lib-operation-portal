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
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "tbl_threshold_configuration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThresholdConfiguration extends JpaEntity<ThresholdConfigurationId> {

    @EmbeddedId
    private ThresholdConfigurationId thresholdConfigurationId;

    @Column(name = "scope_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ThresholdScopeType scopeType;


    @Column(name = "dfsp_id")
    private String dfspId;

    @Column(name = "threshold_enabled", nullable = false)
    private boolean thresholdEnabled;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NdcConfigurationStatus status;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public ThresholdConfiguration(ThresholdScopeType scopeType,
                                  String dfspId,
                                  boolean thresholdEnabled,
                                  String createdBy) {

        Objects.requireNonNull(scopeType, "scopeType is required");
        Objects.requireNonNull(createdBy, "createdBy is required");
        validateScope(scopeType, dfspId);

        this.thresholdConfigurationId = new ThresholdConfigurationId(Snowflake.get().nextId());
        this.scopeType = scopeType;
        this.dfspId = dfspId;
        this.thresholdEnabled = thresholdEnabled;
        this.status = NdcConfigurationStatus.ACTIVE;
        this.createdBy = createdBy;
    }

    public void update(boolean thresholdEnabled,
                       NdcConfigurationStatus status,
                       String updatedBy) {

        this.thresholdEnabled = thresholdEnabled;
        this.status = Objects.requireNonNull(status, "status is required");
        this.updatedBy = Objects.requireNonNull(updatedBy, "updatedBy is required");
    }

    public boolean isActiveAndEnabled() {

        return this.thresholdEnabled && this.status == NdcConfigurationStatus.ACTIVE;
    }

    private static void validateScope(ThresholdScopeType scopeType, String dfspId) {

        if (scopeType == ThresholdScopeType.SCHEME
            && dfspId != null
            && !dfspId.isBlank()) {

            throw new IllegalArgumentException("SCHEME configuration cannot have dfspId");
        }

        if (scopeType == ThresholdScopeType.DFSP
            && (dfspId == null || dfspId.isBlank())) {

            throw new IllegalArgumentException("DFSP configuration requires dfspId");
        }
    }

    @Override
    public ThresholdConfigurationId getId() {

        return this.thresholdConfigurationId;
    }
}
