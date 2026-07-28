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
import com.thitsaworks.operation_portal.component.common.type.RevenueRemainderRecipient;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaInstantConverter;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.time.Instant;

@Entity
@Table(name = "tbl_revenue_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RevenueConfig extends JpaEntity<RevenueConfigId> {

    @EmbeddedId
    protected RevenueConfigId revenueConfigId;

    @Column(
        name = "tax_code_id",
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
        name = "responsible_ministry_code",
        nullable = false,
        length = 50)
    protected String responsibleMinistryCode;

    @Column(
        name = "third_party_provider_code",
        length = 50)
    protected String thirdPartyProviderCode;

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

    @Column(name = "effective_date")
    @Convert(converter = JpaInstantConverter.class)
    protected Instant effectiveDate;

    @Column(name = "responded_date")
    @Convert(converter = JpaInstantConverter.class)
    protected Instant respondedDate;

    public RevenueConfig(String taxCodeId,
                         String taxCodeDescription,
                         RevenueConfigCategory category,
                         String responsibleMinistryCode,
                         String thirdPartyProviderCode,
                         BigDecimal golPercentage,
                         BigDecimal ministryPercentage,
                         BigDecimal thirdPartyPercentage,
                         BigDecimal sendingDfspPercentage,
                         UserId createdBy,
                         Instant effectiveDate,
                         RevenueConfigStatus status,
                         Instant respondedDate) {

        this.revenueConfigId = new RevenueConfigId(Snowflake.get().nextId());
        this.taxCodeId(taxCodeId);
        this.taxCodeDescription(taxCodeDescription);
        this.category(category);
        this.responsibleMinistryCode(responsibleMinistryCode);
        this.thirdPartyProviderCode(thirdPartyProviderCode);
        this.golPercentage(golPercentage);
        this.ministryPercentage(ministryPercentage);
        this.thirdPartyPercentage(thirdPartyPercentage);
        this.sendingDfspPercentage(sendingDfspPercentage);
        this.status(status == null ? RevenueConfigStatus.ACTIVE : status);
        this.createdBy = createdBy;
        this.effectiveDate(effectiveDate);
        this.respondedDate(respondedDate);
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

    public RevenueConfig responsibleMinistryCode(String responsibleMinistryCode) {

        Validate.notBlank(responsibleMinistryCode);
        this.responsibleMinistryCode = responsibleMinistryCode;
        return this;
    }

    public RevenueConfig thirdPartyProviderCode(String thirdPartyProviderCode) {

        this.thirdPartyProviderCode = thirdPartyProviderCode;
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

    public BigDecimal percentageFor(RevenueRemainderRecipient recipient) {

        Validate.notNull(recipient);
        return switch (recipient) {
            case GOL_GRA -> this.golPercentage;
            case MINISTRY -> this.ministryPercentage;
            case THIRD_PARTY -> this.thirdPartyPercentage;
            case DFSP -> this.sendingDfspPercentage;
        };
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

    public RevenueConfig effectiveDate(Instant effectiveDate) {

        this.effectiveDate = effectiveDate;
        return this;
    }

    public RevenueConfig respondedDate(Instant respondedDate) {

        this.respondedDate = respondedDate;
        return this;
    }

    public RevenueConfig update(String taxCodeId,
                                String taxCodeDescription,
                                RevenueConfigCategory category,
                                String responsibleMinistryCode,
                                String thirdPartyProviderCode,
                                BigDecimal golPercentage,
                                BigDecimal ministryPercentage,
                                BigDecimal thirdPartyPercentage,
                                BigDecimal sendingDfspPercentage,
                                UserId updatedBy,
                                Instant effectiveDate,
                                Instant respondedDate) {

        return this
                   .taxCodeId(taxCodeId)
                   .taxCodeDescription(taxCodeDescription)
                   .category(category)
                   .responsibleMinistryCode(responsibleMinistryCode)
                   .thirdPartyProviderCode(thirdPartyProviderCode)
                   .golPercentage(golPercentage)
                   .ministryPercentage(ministryPercentage)
                   .thirdPartyPercentage(thirdPartyPercentage)
                   .sendingDfspPercentage(sendingDfspPercentage)
                   .updatedBy(updatedBy)
                   .effectiveDate(effectiveDate)
                   .respondedDate(respondedDate);
    }

}
