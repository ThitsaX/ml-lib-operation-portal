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

import com.thitsaworks.operation_portal.component.common.identifier.RevenueTransactionId;
import com.thitsaworks.operation_portal.component.common.type.TransactionState;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_transaction")
@Getter
@NoArgsConstructor
public class RevenueTransaction extends JpaEntity<RevenueTransactionId> {

    @EmbeddedId
    protected RevenueTransactionId revenueTransactionId;

    @Column(name = "hub_transaction_id")
    protected String hubTransactionId;

    @Column(name = "tin")
    protected String tin;

    @Column(name = "tax_payer_name")
    protected String taxPayerName;

    @Column(name = "bill_number")
    protected String billNumber;

    @Column(name = "bill_date")
    protected String billDate;

    @Column(name = "total_amount")
    protected BigDecimal totalAmount;

    @Column(name = "amount_currency")
    protected String amountCurrency;

    @Column(name = "sent_currency")
    protected String sentCurrency;

    @Column(name = "rate_exchange")
    protected BigDecimal rateExchange;

    @Column(name = "receipt_number")
    protected String receiptNumber;

    @Column(name = "sender_dfsp_id")
    protected String senderDfspId;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    protected TransactionState state;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "transaction", orphanRemoval = true, fetch = FetchType.LAZY)
    protected Set<RevenueTransactionDetail> transactionDetails = new HashSet<>();

    public RevenueTransaction(String hubTransactionId,
                              String tin,
                              String taxPayerName,
                              String billNumber,
                              String billDate,
                              BigDecimal totalAmount,
                              String amountCurrency,
                              String sentCurrency,
                              BigDecimal rateExchange,
                              String senderDfspId,
                              TransactionState state) {

        Validate.notNull(totalAmount);
        Validate.notBlank(amountCurrency);
        Validate.notNull(state);

        this.revenueTransactionId = new RevenueTransactionId(Snowflake.get().nextId());
        this.hubTransactionId = hubTransactionId;
        this.tin = tin;
        this.taxPayerName = taxPayerName;
        this.billNumber = billNumber;
        this.billDate = billDate;
        this.totalAmount = totalAmount;
        this.amountCurrency = amountCurrency;
        this.sentCurrency = sentCurrency;
        this.rateExchange = rateExchange;
        this.senderDfspId = senderDfspId;
        this.state = state;
    }

    @Override
    public RevenueTransactionId getId() {

        return this.revenueTransactionId;
    }

    public RevenueTransaction addTransactionDetail(String taxCode,
                                                   String taxDescription,
                                                   BigDecimal taxAmount,
                                                   BigDecimal taxAmountCh,
                                                   BigDecimal calculatedAmount,
                                                   String category,
                                                   String responsibleMinistryCode,
                                                   String thirdPartyCode,
                                                   BigDecimal golPercentage,
                                                   BigDecimal golAmount,
                                                   BigDecimal ministryPercent,
                                                   BigDecimal ministryAmount,
                                                   BigDecimal thirdPartyPercent,
                                                   BigDecimal thirdPartyAmount,
                                                   BigDecimal sendingDfspCommissionPercent,
                                                   BigDecimal sendingDfspCommissionAmount) {

        this.transactionDetails.add(new RevenueTransactionDetail(this,
                                                                 taxCode,
                                                                 taxDescription,
                                                                 taxAmount,
                                                                 taxAmountCh,
                                                                 calculatedAmount,
                                                                 category,
                                                                 responsibleMinistryCode,
                                                                 thirdPartyCode,
                                                                 golPercentage,
                                                                 golAmount,
                                                                 ministryPercent,
                                                                 ministryAmount,
                                                                 thirdPartyPercent,
                                                                 thirdPartyAmount,
                                                                 sendingDfspCommissionPercent,
                                                                 sendingDfspCommissionAmount));

        return this;
    }

    public RevenueTransaction state(TransactionState state) {

        Validate.notNull(state);
        this.state = state;

        return this;
    }

    public RevenueTransaction receiptNumber(String receiptNumber) {

        this.receiptNumber = receiptNumber;

        return this;
    }

    public RevenueTransaction hubTransactionId(String hubTransactionId) {

        Validate.notBlank(hubTransactionId);
        this.hubTransactionId = hubTransactionId;

        return this;
    }

    public RevenueTransaction tin(String tin) {

        this.tin = tin;

        return this;
    }

    public RevenueTransaction taxPayerName(String taxPayerName) {

        this.taxPayerName = taxPayerName;

        return this;
    }

    public RevenueTransaction billNumber(String billNumber) {

        this.billNumber = billNumber;

        return this;
    }

    public RevenueTransaction billDate(String billDate) {

        this.billDate = billDate;

        return this;
    }

    public RevenueTransaction totalAmount(BigDecimal totalAmount) {

        Validate.notNull(totalAmount);
        this.totalAmount = totalAmount;

        return this;
    }

    public RevenueTransaction amountCurrency(String amountCurrency) {

        Validate.notBlank(amountCurrency);
        this.amountCurrency = amountCurrency;

        return this;
    }

    public RevenueTransaction sentCurrency(String sentCurrency) {

        this.sentCurrency = sentCurrency;

        return this;
    }

    public RevenueTransaction rateExchange(BigDecimal rateExchange) {

        this.rateExchange = rateExchange;

        return this;
    }

    public RevenueTransaction senderDfspId(String senderDfspId) {

        this.senderDfspId = senderDfspId;

        return this;
    }

    public RevenueTransaction clearTransactionDetails() {

        this.transactionDetails.clear();

        return this;
    }

}
