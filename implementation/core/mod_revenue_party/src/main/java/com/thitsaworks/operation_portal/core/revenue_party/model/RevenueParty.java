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

import com.thitsaworks.operation_portal.component.common.identifier.RevenuePartyId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_revenue_party")
@Getter
@NoArgsConstructor
public class RevenueParty extends JpaEntity<RevenuePartyId> {

    @EmbeddedId
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
    @AttributeOverride(name = "id", column = @Column(name = "created_by", updatable = false))
    protected UserId createdBy;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "updated_by"))
    protected UserId updatedBy;

    public RevenueParty(String partyCode,
                        String partyName,
                        String partyType,
                        String description,
                        UserId createdBy) {

        this.revenuePartyId = new RevenuePartyId(Snowflake.get().nextId());
        this.partyCode(partyCode);
        this.partyName(partyName);
        this.partyType(partyType);
        this.description(description);
        this.isActive(true);
        this.createdBy(createdBy);
    }

    @Override
    public RevenuePartyId getId() {

        return this.revenuePartyId;
    }

    public RevenueParty partyCode(String partyCode) {

        this.partyCode = partyCode;

        return this;
    }

    public RevenueParty partyName(String partyName) {

        this.partyName = partyName;

        return this;
    }

    public RevenueParty partyType(String partyType) {

        this.partyType = partyType;

        return this;
    }

    public RevenueParty description(String description) {

        this.description = description;

        return this;
    }

    public RevenueParty isActive(boolean isActive) {

        this.isActive = isActive;

        return this;
    }

    public RevenueParty createdBy(UserId createdBy) {

        this.createdBy = createdBy;

        return this;
    }

    public RevenueParty updatedBy(UserId updatedBy) {

        this.updatedBy = updatedBy;

        return this;
    }

}
