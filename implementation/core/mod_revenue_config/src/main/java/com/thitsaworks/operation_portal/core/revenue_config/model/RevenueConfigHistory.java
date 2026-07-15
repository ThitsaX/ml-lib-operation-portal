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

package com.thitsaworks.operation_portal.core.revenue_config.model;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigHistoryId;
import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
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

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_revenue_config_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RevenueConfigHistory extends JpaEntity<RevenueConfigHistoryId> {

    @EmbeddedId
    private RevenueConfigHistoryId revenueConfigHistoryId;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(
            name = "revenue_config_id",
            nullable = false))
    private RevenueConfigId revenueConfigId;

    @Column(
        name = "tax_code_id",
        nullable = false,
        length = 50)
    private String taxCodeId;

    @Column(
        name = "tax_code_description",
        nullable = false)
    private String taxCodeDescription;

    @Column(
        name = "category",
        nullable = false,
        length = 20)
    @Enumerated(EnumType.STRING)
    private RevenueConfigCategory category;

    @Column(
        name = "responsible_ministry_id",
        nullable = false)
    private Long responsibleMinistryId;

    @Column(name = "third_party_provider_id")
    private Long thirdPartyProviderId;

    @Column(
        name = "gol_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    private BigDecimal golPercentage;

    @Column(
        name = "ministry_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    private BigDecimal ministryPercentage;

    @Column(
        name = "third_party_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    private BigDecimal thirdPartyPercentage;

    @Column(
        name = "sending_dfsp_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    private BigDecimal sendingDfspPercentage;

    @Column(
        name = "status",
        nullable = false,
        length = 20)
    @Enumerated(EnumType.STRING)
    private RevenueConfigStatus status;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "created_by"))
    private UserId createdBy;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "updated_by"))
    private UserId updatedBy;

    public RevenueConfigHistory(RevenueConfig revenueConfig) {

        this.revenueConfigHistoryId = new RevenueConfigHistoryId(Snowflake.get().nextId());
        this.revenueConfigId = revenueConfig.getRevenueConfigId();
        this.taxCodeId = revenueConfig.getTaxCodeId();
        this.taxCodeDescription = revenueConfig.getTaxCodeDescription();
        this.category = revenueConfig.getCategory();
        this.responsibleMinistryId = revenueConfig.getResponsibleMinistryId();
        this.thirdPartyProviderId = revenueConfig.getThirdPartyProviderId();
        this.golPercentage = revenueConfig.getGolPercentage();
        this.ministryPercentage = revenueConfig.getMinistryPercentage();
        this.thirdPartyPercentage = revenueConfig.getThirdPartyPercentage();
        this.sendingDfspPercentage = revenueConfig.getSendingDfspPercentage();
        this.status = revenueConfig.getStatus();
        this.createdBy = revenueConfig.getCreatedBy();
        this.updatedBy = revenueConfig.getUpdatedBy();
        this.setCreatedAt(revenueConfig.getCreatedAt());
        this.setUpdatedAt(revenueConfig.getUpdatedAt());
    }

    @Override
    public RevenueConfigHistoryId getId() {

        return this.revenueConfigHistoryId;
    }

}
