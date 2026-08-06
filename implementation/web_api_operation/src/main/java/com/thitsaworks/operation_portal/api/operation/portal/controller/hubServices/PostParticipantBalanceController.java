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
package com.thitsaworks.operation_portal.api.operation.portal.controller.hubServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.fspiop.model.Currency;
import com.thitsaworks.operation_portal.component.fspiop.model.ExtensionList;
import com.thitsaworks.operation_portal.component.fspiop.model.Money;
import com.thitsaworks.operation_portal.component.misc.util.TransferIdGenerator;
import com.thitsaworks.operation_portal.core.hub_services.ParticipantHubClient;
import com.thitsaworks.operation_portal.core.hub_services.api.PostParticipantBalance;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@Validated
@RequiredArgsConstructor
public class PostParticipantBalanceController {

    private static final Logger LOG = LoggerFactory.getLogger(PostParticipantBalanceController.class);

    private static final String MOJALOOP_AMOUNT_FORMAT =
        "^([0]|([1-9][0-9]{0,17}))([.][0-9]{0,3}[1-9])?$";

    private final ParticipantHubClient participantHubClient;

    private final ObjectMapper objectMapper;

    @PostMapping(
        value = "/secured/postParticipantBalance/{participantId}/accounts/{accountId}",
        consumes = "application/json",
        produces = "application/json")
    public ResponseEntity<Response> execute(
        @NotBlank @PathVariable("participantId") String participantId,
        @Positive @PathVariable("accountId") Long accountId,
        @Valid @RequestBody Request request)
        throws JsonProcessingException, HubServicesException {

        String transferId = TransferIdGenerator.generateTransferId();

        Money money = new Money()
                          .currency(request.currency())
                          .amount(request.amount());

        PostParticipantBalance.Request hubRequest = new PostParticipantBalance.Request(
            transferId,
            request.externalReference(),
            request.action(),
            request.reason(),
            money,
            request.extensionList());

        LOG.info(
            "Post Participant Balance Request : participantId : [{}], accountId : [{}], request : [{}]",
            participantId, accountId, this.objectMapper.writeValueAsString(hubRequest));

        this.participantHubClient.postParticipantBalance(
            participantId, accountId.toString(), hubRequest);

        var response = new Response(transferId, "ACCEPTED");

        LOG.info("Post Participant Balance Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @JsonProperty("externalReference")
        String externalReference,

        @NotBlank
        @Pattern(
            regexp = "^(recordFundsIn|recordFundsOutPrepareReserve)$",
            message = "Action must be recordFundsIn or recordFundsOutPrepareReserve.")
        @JsonProperty("action")
        String action,

        @NotBlank
        @JsonProperty("reason")
        String reason,

        @NotBlank
        @DecimalMin(value = "0.0001", message = "Amount must be greater than zero.")
        @Pattern(
            regexp = MOJALOOP_AMOUNT_FORMAT,
            message = "Amount must use the valid Mojaloop amount format.")
        @JsonProperty("amount")
        String amount,

        @NotNull
        @JsonProperty("currency")
        Currency currency,

        @JsonProperty("extensionList")
        ExtensionList extensionList) implements Serializable {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("transferId") String transferId,
                           @JsonProperty("status") String status) implements Serializable {

    }

}
