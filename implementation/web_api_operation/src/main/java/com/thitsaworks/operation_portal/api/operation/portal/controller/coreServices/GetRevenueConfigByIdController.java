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

package com.thitsaworks.operation_portal.api.operation.portal.controller.coreServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenueConfigById;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class GetRevenueConfigByIdController {

    private static final Logger LOG = LoggerFactory.getLogger(GetRevenueConfigByIdController.class);

    private final GetRevenueConfigById getRevenueConfigById;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getRevenueConfigById")
    public ResponseEntity<Response> execute(@Valid @RequestParam Long revenueConfigurationId)
        throws DomainException, JsonProcessingException {

        LOG.info(
            "Get Revenue Config by Id Request : revenueConfigurationId = [{}]",
            revenueConfigurationId);

        GetRevenueConfigById.Output output = this.getRevenueConfigById.execute(
            new GetRevenueConfigById.Input(new RevenueConfigId(revenueConfigurationId)));

        Response response = Response.from(output.revenueConfig());

        LOG.info(
            "Get Revenue Config by Id Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("id") String id,
                           @JsonProperty("taxCodeId") String taxCodeId,
                           @JsonProperty("taxCodeDescription") String taxCodeDescription,
                           @JsonProperty("category") String category,
                           @JsonProperty("responsibleMinistryCode") String responsibleMinistryCode,
                           @JsonProperty("responsibleMinistryName") String responsibleMinistryName,
                           @JsonProperty("thirdPartyProviderCode") String thirdPartyProviderCode,
                           @JsonProperty("thirdPartyProviderName") String thirdPartyProviderName,
                           @JsonProperty("golPercentage") BigDecimal golPercentage,
                           @JsonProperty("ministryPercentage") BigDecimal ministryPercentage,
                           @JsonProperty("thirdPartyPercentage") BigDecimal thirdPartyPercentage,
                           @JsonProperty("sendingDfspPercentage") BigDecimal sendingDfspPercentage,
                           @JsonProperty("status") String status,
                           @JsonProperty("createdAt") Long createdAt,
                           @JsonProperty("createdBy") String createdBy,
                           @JsonProperty("updatedAt") Long updatedAt,
                           @JsonProperty("updatedBy") String updatedBy) {

        public static Response from(GetRevenueConfigById.RevenueConfig revenueConfig) {

            return new Response(
                revenueConfig.id().toString(), revenueConfig.taxCodeId(), revenueConfig.taxCodeDescription(),
                revenueConfig.category(),
                revenueConfig.responsibleMinistryCode(),
                revenueConfig.responsibleMinistryName(),
                revenueConfig.thirdPartyProviderCode(),
                revenueConfig.thirdPartyProviderName(), revenueConfig.golPercentage(),
                revenueConfig.ministryPercentage(), revenueConfig.thirdPartyPercentage(),
                revenueConfig.sendingDfspPercentage(), revenueConfig.status().name(),
                revenueConfig.createdAt(), revenueConfig.createdBy(), revenueConfig.updatedAt(),
                revenueConfig.updatedBy());
        }

    }

}
