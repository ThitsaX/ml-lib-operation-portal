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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenueRoundingPolicy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class GetRevenueRoundingPolicyController {

    private static final Logger LOG =
        LoggerFactory.getLogger(GetRevenueRoundingPolicyController.class);

    private final GetRevenueRoundingPolicy getRevenueRoundingPolicy;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getRevenueRoundingPolicy")
    public ResponseEntity<Response> execute()
        throws DomainException, JsonProcessingException {

        LOG.info("Get Revenue Rounding Policy Request");

        GetRevenueRoundingPolicy.Output output = this.getRevenueRoundingPolicy.execute(
            new GetRevenueRoundingPolicy.Input());
        Response response = Response.from(output.revenueRoundingPolicy());

        LOG.info(
            "Get Revenue Rounding Policy Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("revenueRoundingPolicyId")
                           String revenueRoundingPolicyId,
                           @JsonProperty("roundingMode") String roundingMode,
                           @JsonProperty("remainderRecipient") String remainderRecipient,
                           @JsonProperty("createdAt") Long createdAt,
                           @JsonProperty("createdBy") String createdBy)
        implements Serializable {

        private static Response from(
            GetRevenueRoundingPolicy.RevenueRoundingPolicy policy) {

            if (policy == null) {
                return new Response(null, null, null, null, null);
            }

            return new Response(
                policy.revenueRoundingPolicyId().getId().toString(),
                policy.roundingMode().name(), policy.remainderRecipient().name(),
                policy.createdAt() == null ? null : policy.createdAt().getEpochSecond(),
                policy.createdBy());
        }
    }
}
