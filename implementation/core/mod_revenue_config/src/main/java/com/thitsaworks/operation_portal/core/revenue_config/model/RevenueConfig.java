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
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_revenue_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RevenueConfig extends JpaEntity<RevenueConfigId> {

    @EmbeddedId
    protected RevenueConfigId revenueConfigId;

    @Column(
        name = "tax_code_id",
        unique = true,
        nullable = false,
        length = 50)
    protected String taxCodeId;

    @Column(
        name = "tax_code_description",
        nullable = false)
    protected String taxCodeDescription;

    @Column(
        name = "category",
        nullable = false,
        length = 20)
    @Enumerated(EnumType.STRING)
    protected RevenueConfigCategory category;

    @Column(
        name = "responsible_ministry_id",
        nullable = false)
    protected Long responsibleMinistryId;

    @Column(name = "third_party_provider_id")
    protected Long thirdPartyProviderId;

    @Column(
        name = "gol_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    protected BigDecimal golPercentage;

    @Column(
        name = "ministry_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    protected BigDecimal ministryPercentage;

    @Column(
        name = "third_party_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    protected BigDecimal thirdPartyPercentage;

    @Column(
        name = "sending_dfsp_percentage",
        precision = 5,
        scale = 2,
        nullable = false)
    protected BigDecimal sendingDfspPercentage;

    @Column(
        name = "status",
        nullable = false,
        length = 20)
    @Enumerated(EnumType.STRING)
    protected RevenueConfigStatus status;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "created_by"))
    protected UserId createdBy;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "updated_by"))
    protected UserId updatedBy;

    public RevenueConfig(String taxCodeId,
                         String taxCodeDescription,
                         RevenueConfigCategory category,
                         Long responsibleMinistryId,
                         Long thirdPartyProviderId,
                         BigDecimal golPercentage,
                         BigDecimal ministryPercentage,
                         BigDecimal thirdPartyPercentage,
                         BigDecimal sendingDfspPercentage,
                         UserId updatedBy) {

        this.revenueConfigId = new RevenueConfigId(Snowflake.get().nextId());
        this.taxCodeId(taxCodeId);
        this.taxCodeDescription(taxCodeDescription);
        this.category(category);
        this.responsibleMinistryId(responsibleMinistryId);
        this.thirdPartyProviderId(thirdPartyProviderId);
        this.golPercentage(golPercentage);
        this.ministryPercentage(ministryPercentage);
        this.thirdPartyPercentage(thirdPartyPercentage);
        this.sendingDfspPercentage(sendingDfspPercentage);
        this.status = RevenueConfigStatus.ACTIVE;
        this.createdBy = updatedBy;
        this.updatedBy = updatedBy;
    }

    @Override
    public RevenueConfigId getId() {

        return this.revenueConfigId;
    }

    public RevenueConfig taxCodeId(String taxCodeId) {

        Validate.notBlank(taxCodeId);
        this.taxCodeId = taxCodeId;
        return this;
    }

    public RevenueConfig taxCodeDescription(String taxCodeDescription) {

        Validate.notBlank(taxCodeDescription);
        this.taxCodeDescription = taxCodeDescription;
        return this;
    }

    public RevenueConfig category(RevenueConfigCategory category) {

        Validate.notNull(category);
        this.category = category;
        return this;
    }

    public RevenueConfig responsibleMinistryId(Long responsibleMinistryId) {

        Validate.notNull(responsibleMinistryId);
        this.responsibleMinistryId = responsibleMinistryId;
        return this;
    }

    public RevenueConfig thirdPartyProviderId(Long thirdPartyProviderId) {

        this.thirdPartyProviderId = thirdPartyProviderId;
        return this;
    }

    public RevenueConfig golPercentage(BigDecimal golPercentage) {

        this.golPercentage = golPercentage;
        return this;
    }

    public RevenueConfig ministryPercentage(BigDecimal ministryPercentage) {

        this.ministryPercentage = ministryPercentage;
        return this;
    }

    public RevenueConfig thirdPartyPercentage(BigDecimal thirdPartyPercentage) {

        this.thirdPartyPercentage = thirdPartyPercentage;
        return this;
    }

    public RevenueConfig sendingDfspPercentage(BigDecimal sendingDfspPercentage) {

        this.sendingDfspPercentage = sendingDfspPercentage;
        return this;
    }

    public RevenueConfig status(RevenueConfigStatus status) {

        Validate.notNull(status);
        this.status = status;
        return this;
    }

    public RevenueConfig updatedBy(UserId updatedBy) {

        this.updatedBy = updatedBy;
        return this;
    }

    public RevenueConfig update(String taxCodeId,
                                String taxCodeDescription,
                                RevenueConfigCategory category,
                                Long responsibleMinistryId,
                                Long thirdPartyProviderId,
                                BigDecimal golPercentage,
                                BigDecimal ministryPercentage,
                                BigDecimal thirdPartyPercentage,
                                BigDecimal sendingDfspPercentage,
                                UserId updatedBy) {

        return this
                   .taxCodeId(taxCodeId)
                   .taxCodeDescription(taxCodeDescription)
                   .category(category)
                   .responsibleMinistryId(responsibleMinistryId)
                   .thirdPartyProviderId(thirdPartyProviderId)
                   .golPercentage(golPercentage)
                   .ministryPercentage(ministryPercentage)
                   .thirdPartyPercentage(thirdPartyPercentage)
                   .sendingDfspPercentage(sendingDfspPercentage)
                   .updatedBy(updatedBy);
    }

}
