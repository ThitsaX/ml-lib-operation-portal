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
package com.thitsaworks.operation_portal.core.revenue_party.model;

import com.thitsaworks.operation_portal.component.common.identifier.RevenuePartyHistoryId;
import com.thitsaworks.operation_portal.component.common.identifier.RevenuePartyId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenuePartyActionType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "tbl_revenue_party_history")
@Getter
@NoArgsConstructor
public class RevenuePartyHistory {

    @EmbeddedId
    protected RevenuePartyHistoryId revenuePartyHistoryId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "revenue_party_id"))
    protected RevenuePartyId revenuePartyId;

    @Column(name = "party_code")
    protected String partyCode;

    @Column(name = "party_name")
    protected String partyName;

    @Column(name = "party_type")
    protected String partyType;

    @Column(name = "description")
    protected String description;

    @Column(name = "is_active")
    protected boolean isActive;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "created_by"))
    protected UserId createdBy;

    @Column(name = "created_date")
    @Convert(converter = JpaInstantConverter.class)
    protected Instant createdDate;

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    protected RevenuePartyActionType actionType;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "changed_by"))
    protected UserId changedBy;

    @Column(name = "changed_date")
    @Convert(converter = JpaInstantConverter.class)
    protected Instant changedDate;

    public RevenuePartyHistory(RevenueParty revenueParty,
                               RevenuePartyActionType actionType,
                               UserId changedBy) {

        this.revenuePartyHistoryId = new RevenuePartyHistoryId(Snowflake.get().nextId());
        this.revenuePartyId = revenueParty.getRevenuePartyId();
        this.partyCode = revenueParty.getPartyCode();
        this.partyName = revenueParty.getPartyName();
        this.partyType = revenueParty.getPartyType();
        this.description = revenueParty.getDescription();
        this.isActive = revenueParty.isActive();
        this.createdBy = revenueParty.getCreatedBy();
        this.createdDate = revenueParty.getCreatedAt();
        this.actionType = actionType;
        this.changedBy = changedBy;
        this.changedDate = Instant.now();
    }

}
