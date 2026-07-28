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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.core.revenue_config.engine.RevenueEngine;
import com.thitsaworks.operation_portal.usecase.operation_portal.CalculateRevenueSplit;
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

@RestController
@RequiredArgsConstructor
public class CalculateRevenueSplitController {

    private static final Logger LOG = LoggerFactory.getLogger(
        CalculateRevenueSplitController.class);

    private final CalculateRevenueSplit calculateRevenueSplit;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/calculateRevenueSplit")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info(
            "Calculate Revenue Split Request : [{}]",
            this.objectMapper.writeValueAsString(request));

        CalculateRevenueSplit.Output output = this.calculateRevenueSplit.execute(
            new CalculateRevenueSplit.Input(request.taxCodeId(), request.amount()));
        Response response = Response.from(output.revenueSplit());

        LOG.info(
            "Calculate Revenue Split Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @NotBlank @JsonProperty("taxCodeId") String taxCodeId,
                          @NotNull @JsonProperty("amount") BigDecimal amount)
        implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("revenueConfigId") String revenueConfigId,
                           @JsonProperty("taxCodeId") String taxCodeId,
                           @JsonProperty("taxCodeDescription") String taxCodeDescription,
                           @JsonProperty("revenueConfigCategory") String revenueConfigCategory,
                           @JsonProperty("responsibleMinistryCode") String responsibleMinistryCode,
                           @JsonProperty("thirdPartyProviderCode") String thirdPartyProviderCode,
                           @JsonProperty("amount") BigDecimal amount,
                           @JsonProperty("golPercentage") BigDecimal golPercentage,
                           @JsonProperty("golAmount") BigDecimal golAmount,
                           @JsonProperty("ministryPercentage") BigDecimal ministryPercentage,
                           @JsonProperty("ministryAmount") BigDecimal ministryAmount,
                           @JsonProperty("thirdPartyPercentage") BigDecimal thirdPartyPercentage,
                           @JsonProperty("thirdPartyAmount") BigDecimal thirdPartyAmount,
                           @JsonProperty("sendingDfspPercentage") BigDecimal sendingDfspPercentage,
                           @JsonProperty("sendingDfspAmount") BigDecimal sendingDfspAmount,
                           @JsonProperty("roundingMode") String roundingMode,
                           @JsonProperty("remainderRecipient") String remainderRecipient)
        implements Serializable {

        private static Response from(RevenueEngine.RevenueSplit split) {

            return new Response(
                split.revenueConfigId().getId().toString(), split.taxCodeId(),
                split.taxCodeDescription(), split.revenueConfigCategory().name(),
                split.responsibleMinistryCode(), split.thirdPartyProviderCode(), split.amount(),
                split.golPercentage(), split.golAmount(), split.ministryPercentage(),
                split.ministryAmount(), split.thirdPartyPercentage(), split.thirdPartyAmount(),
                split.sendingDfspPercentage(), split.sendingDfspAmount(), split.roundMode(),
                split.remainderRecipient());
        }

    }

}
