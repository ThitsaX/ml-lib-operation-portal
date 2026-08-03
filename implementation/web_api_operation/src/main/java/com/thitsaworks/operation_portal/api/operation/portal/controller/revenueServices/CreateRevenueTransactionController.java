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
package com.thitsaworks.operation_portal.api.operation.portal.controller.revenueServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.type.TransactionState;
import com.thitsaworks.operation_portal.core.revenue_transaction.data.RevenueTransactionDetailInput;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateRevenueTransaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CreateRevenueTransactionController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateRevenueTransactionController.class);

    private final CreateRevenueTransaction createRevenueTransaction;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/createRevenueTransaction")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request) throws Exception {

        LOG.info("Create Revenue Transaction Request : [{}]",
                 this.objectMapper.writeValueAsString(request));

        var transactionDetails = request.transactionDetails() == null
                                     ? List.<RevenueTransactionDetailInput>of()
                                     : request.transactionDetails().stream()
                                              .map(TransactionDetailRequest::toInput)
                                              .toList();

        var output = this.createRevenueTransaction.execute(new CreateRevenueTransaction.Input(
                request.hubTransactionId(), request.tin(),
                request.taxPayerName(), request.billNumber(),
                request.billDate(),
                request.totalAmount(), request.amountCurrency(), request.sentCurrency(), request.rateExchange(),
                request.senderDfspId(), TransactionState.valueOf(request.state().toUpperCase()),
                transactionDetails));

        var response = new Response(output.created(), output.revenueTransactionId().getId().toString());

        LOG.info("Create Revenue Transaction Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@JsonProperty("hubTransactionId") String hubTransactionId,
                          @JsonProperty("tin") String tin,
                          @JsonProperty("taxPayerName") String taxPayerName,
                          @JsonProperty("billNumber") String billNumber,
                          @JsonProperty("billDate") String billDate,
                          @NotNull @JsonProperty("totalAmount") BigDecimal totalAmount,
                          @NotBlank @JsonProperty("amountCurrency") String amountCurrency,
                          @JsonProperty("sentCurrency") String sentCurrency,
                          @JsonProperty("rateExchange") BigDecimal rateExchange,
                          @JsonProperty("senderDfspId") String senderDfspId,
                          @NotBlank @JsonProperty("state") String state,
                          @Valid @JsonProperty("transactionDetails")
                          List<TransactionDetailRequest> transactionDetails) implements Serializable {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionDetailRequest(
            @NotBlank @JsonProperty("taxCode") String taxCode,
            @JsonProperty("taxDescription") String taxDescription,
            @NotNull @JsonProperty("taxAmount") BigDecimal taxAmount,
            @JsonProperty("taxAmountCh") BigDecimal taxAmountCh,
            @JsonProperty("category") String category,
            @JsonProperty("responsibleMinistryCode") String responsibleMinistryCode,
            @JsonProperty("thirdPartyCode") String thirdPartyCode,
            @JsonProperty("golPercentage") BigDecimal golPercentage,
            @JsonProperty("golAmount") BigDecimal golAmount,
            @JsonProperty("ministryPercent") BigDecimal ministryPercent,
            @JsonProperty("ministryAmount") BigDecimal ministryAmount,
            @JsonProperty("thirdPartyPercent") BigDecimal thirdPartyPercent,
            @JsonProperty("thirdPartyAmount") BigDecimal thirdPartyAmount,
            @JsonProperty("sendingDfspCommissionPercent") BigDecimal sendingDfspCommissionPercent,
            @JsonProperty("sendingDfspCommissionAmount") BigDecimal sendingDfspCommissionAmount)
            implements Serializable {

        public RevenueTransactionDetailInput toInput() {

            return new RevenueTransactionDetailInput(
                    this.taxCode, this.taxDescription, this.taxAmount, this.taxAmountCh,
                    null,
                    this.category, this.responsibleMinistryCode, this.thirdPartyCode,
                    this.golPercentage, this.golAmount, this.ministryPercent, this.ministryAmount,
                    this.thirdPartyPercent, this.thirdPartyAmount,
                    this.sendingDfspCommissionPercent, this.sendingDfspCommissionAmount);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("created") boolean created,
                           @JsonProperty("revenueTransactionId") String revenueTransactionId)
            implements Serializable {
    }
}
