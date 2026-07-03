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
package com.thitsaworks.operation_portal.core.participant.model;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tbl_participant_ndc")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipantNDC extends JpaEntity<ParticipantNDCId> {

    @EmbeddedId
    protected ParticipantNDCId participantNDCId;

    @Column(name = "participant_name")
    protected String participantName;

    @Column(name = "currency")
    protected String currency;

    @Column(
        name = "ndc_percent",
            precision = 7,
        scale = 4)
    protected BigDecimal ndcPercent;

    @Column(
            name = "ndc_amount",
            precision = 18,
            scale = 4)
    protected BigDecimal ndcAmount;

    @Column(
            name = "balance",
            precision = 18,
            scale = 4)
    protected BigDecimal balance;

    @Column(name = "made_by")
    protected String madeBy;

    public ParticipantNDC(String participantName,
                          String currency,
                          BigDecimal ndcPercent,
                          BigDecimal ndcAmount,
                          BigDecimal balance,
                          String madeBy) {

        this.participantNDCId = new ParticipantNDCId(Snowflake.get()
                                                              .nextId());
        this.participantName(participantName);
        this.currency(currency);
        this.ndcPercent(ndcPercent);
        this.ndcAmount(ndcAmount);
        this.balance(balance);
        this.madeBy(madeBy);
    }

    public ParticipantNDC participantName(String participantName) {

        this.participantName = participantName;
        return this;
    }

    public ParticipantNDC currency(String currency) {

        this.currency = currency;
        return this;
    }

    public ParticipantNDC ndcPercent(BigDecimal ndcPercent) {

        this.ndcPercent = ndcPercent;
        return this;
    }

    public ParticipantNDC ndcAmount(BigDecimal ndcAmount) {

        this.ndcAmount = ndcAmount;
        return this;
    }

    public ParticipantNDC balance(BigDecimal balance) {

        this.balance = balance;
        return this;
    }

    public ParticipantNDC madeBy(String madeBy) {

        this.madeBy = madeBy;
        return this;
    }

    public ParticipantNDC updatedAt() {

        this.updatedAt = Instant.now();
        return this;
    }

    @Override
    public ParticipantNDCId getId() {

        return this.participantNDCId;
    }

}
