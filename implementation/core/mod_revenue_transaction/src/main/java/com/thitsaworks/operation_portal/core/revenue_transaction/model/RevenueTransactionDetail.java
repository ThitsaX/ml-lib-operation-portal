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
package com.thitsaworks.operation_portal.core.revenue_transaction.model;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueTransactionDetailId;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_transaction_detail")
@Getter
@NoArgsConstructor
public class RevenueTransactionDetail extends JpaEntity<RevenueTransactionDetailId> {

    @EmbeddedId
    protected RevenueTransactionDetailId revenueTransactionDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    protected RevenueTransaction transaction;

    @Column(name = "tax_code")
    protected String taxCode;

    @Column(name = "tax_description")
    protected String taxDescription;

    @Column(name = "tax_amount")
    protected BigDecimal taxAmount;

    @Column(name = "tax_amount_ch")
    protected BigDecimal taxAmountCh;

    @Column(name = "calculated_amount")
    protected BigDecimal calculatedAmount;

    @Column(name = "category")
    protected String category;

    @Column(name = "responsible_ministry_code")
    protected String responsibleMinistryCode;

    @Column(name = "responsible_ministry_name")
    protected String responsibleMinistryName;

    @Column(name = "third_party_code")
    protected String thirdPartyCode;

    @Column(name = "third_party_name")
    protected String thirdPartyName;

    @Column(name = "gol_percentage")
    protected BigDecimal golPercentage;

    @Column(name = "gol_amount")
    protected BigDecimal golAmount;

    @Column(name = "ministry_percent")
    protected BigDecimal ministryPercent;

    @Column(name = "ministry_amount")
    protected BigDecimal ministryAmount;

    @Column(name = "third_party_percent")
    protected BigDecimal thirdPartyPercent;

    @Column(name = "third_party_amount")
    protected BigDecimal thirdPartyAmount;

    @Column(name = "sending_dfsp_commission_percent")
    protected BigDecimal sendingDfspCommissionPercent;

    @Column(name = "sending_dfsp_commission_amount")
    protected BigDecimal sendingDfspCommissionAmount;

    public RevenueTransactionDetail(RevenueTransaction transaction,
                                    String taxCode,
                                    String taxDescription,
                                    BigDecimal taxAmount,
                                    BigDecimal taxAmountCh,
                                    BigDecimal calculatedAmount,
                                    String category,
                                    String responsibleMinistryCode,
                                    String responsibleMinistryName,
                                    String thirdPartyCode,
                                    String thirdPartyName,
                                    BigDecimal golPercentage,
                                    BigDecimal golAmount,
                                    BigDecimal ministryPercent,
                                    BigDecimal ministryAmount,
                                    BigDecimal thirdPartyPercent,
                                    BigDecimal thirdPartyAmount,
                                    BigDecimal sendingDfspCommissionPercent,
                                    BigDecimal sendingDfspCommissionAmount) {

        Validate.notNull(transaction);
        Validate.notBlank(taxCode);
        Validate.notNull(taxAmount);

        this.revenueTransactionDetailId = new RevenueTransactionDetailId(Snowflake.get().nextId());
        this.transaction = transaction;
        this.taxCode = taxCode;
        this.taxDescription = taxDescription;
        this.taxAmount = taxAmount;
        this.taxAmountCh = taxAmountCh;
        this.calculatedAmount = calculatedAmount;
        this.category = category;
        this.responsibleMinistryCode = responsibleMinistryCode;
        this.responsibleMinistryName = responsibleMinistryName;
        this.thirdPartyCode = thirdPartyCode;
        this.thirdPartyName = thirdPartyName;
        this.golPercentage = golPercentage;
        this.golAmount = golAmount;
        this.ministryPercent = ministryPercent;
        this.ministryAmount = ministryAmount;
        this.thirdPartyPercent = thirdPartyPercent;
        this.thirdPartyAmount = thirdPartyAmount;
        this.sendingDfspCommissionPercent = sendingDfspCommissionPercent;
        this.sendingDfspCommissionAmount = sendingDfspCommissionAmount;
    }

    public RevenueTransactionDetail revenueSplit(BigDecimal calculatedAmount,
                                                 String taxCodeDescription,
                                                 String category,
                                                 String responsibleMinistryCode,
                                                 String responsibleMinistryName,
                                                 String thirdPartyCode,
                                                 String thirdPartyName,
                                                 BigDecimal golPercentage,
                                                 BigDecimal golAmount,
                                                 BigDecimal ministryPercent,
                                                 BigDecimal ministryAmount,
                                                 BigDecimal thirdPartyPercent,
                                                 BigDecimal thirdPartyAmount,
                                                 BigDecimal sendingDfspCommissionPercent,
                                                 BigDecimal sendingDfspCommissionAmount) {

        this.calculatedAmount = calculatedAmount;
        this.taxDescription = taxCodeDescription;
        this.category = category;
        this.responsibleMinistryCode = responsibleMinistryCode;
        this.responsibleMinistryName = responsibleMinistryName;
        this.thirdPartyCode = thirdPartyCode;
        this.thirdPartyName = thirdPartyName;
        this.golPercentage = golPercentage;
        this.golAmount = golAmount;
        this.ministryPercent = ministryPercent;
        this.ministryAmount = ministryAmount;
        this.thirdPartyPercent = thirdPartyPercent;
        this.thirdPartyAmount = thirdPartyAmount;
        this.sendingDfspCommissionPercent = sendingDfspCommissionPercent;
        this.sendingDfspCommissionAmount = sendingDfspCommissionAmount;

        return this;
    }

    @Override
    public RevenueTransactionDetailId getId() {

        return this.revenueTransactionDetailId;
    }

}
