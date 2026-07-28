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

import com.thitsaworks.operation_portal.component.common.identifier.RevenueRoundingPolicyId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueRemainderRecipient;
import com.thitsaworks.operation_portal.component.common.type.RevenueRoundingMode;
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

@Entity
@Table(name = "tbl_revenue_rounding_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RevenueRoundingPolicy extends JpaEntity<RevenueRoundingPolicyId> {

    @EmbeddedId
    private RevenueRoundingPolicyId revenueRoundingPolicyId;

    @Column(
        name = "rounding_mode",
        nullable = false,
        length = 20)
    @Enumerated(EnumType.STRING)
    private RevenueRoundingMode roundingMode;

    @Column(
        name = "remainder_recipient",
        nullable = false,
        length = 30)
    @Enumerated(EnumType.STRING)
    private RevenueRemainderRecipient remainderRecipient;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "created_by"))
    private UserId createdBy;

    public RevenueRoundingPolicy(RevenueRoundingMode roundingMode,
                                 RevenueRemainderRecipient remainderRecipient,
                                 UserId createdBy) {

        this.revenueRoundingPolicyId = new RevenueRoundingPolicyId(Snowflake.get().nextId());
        this.roundingMode = Validate.notNull(roundingMode);
        this.remainderRecipient = Validate.notNull(remainderRecipient);
        this.createdBy = Validate.notNull(createdBy);
    }

    @Override
    public RevenueRoundingPolicyId getId() {

        return this.revenueRoundingPolicyId;
    }

}
