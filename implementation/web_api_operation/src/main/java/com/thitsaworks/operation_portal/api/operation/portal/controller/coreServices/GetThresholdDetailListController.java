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
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetThresholdDetail;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetThresholdDetailList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetThresholdDetailListController {

    private static final Logger LOG = LoggerFactory.getLogger(GetThresholdDetailListController.class);

    private final GetThresholdDetailList getThresholdDetailList;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/ndc/thresholdDetails")
    public ResponseEntity<Response> execute(
        @RequestParam(value = "thresholdConfigurationId", required = false) Long thresholdConfigurationId,
        @RequestParam(value = "status", required = false) Boolean status)
        throws DomainException, JsonProcessingException {

        LOG.info("Get NDC Threshold Detail List Request : thresholdConfigurationId=[{}], status=[{}]",
                 thresholdConfigurationId, status);

        GetThresholdDetailList.Output output =
            this.getThresholdDetailList.execute(
                new GetThresholdDetailList.Input(thresholdConfigurationId, status));

        var response = new Response(output.thresholdDetails().stream()
            .map(thresholdDetail -> new ThresholdDetail(
                thresholdDetail.thresholdDetailId(),
                thresholdDetail.thresholdConfigurationId(),
                thresholdDetail.participantCurrencyId(),
                thresholdDetail.dfspId(),
                thresholdDetail.currency(),
                thresholdDetail.visualConfig(),
                thresholdDetail.ndcConfig(),
                thresholdDetail.status()
            )).toList());

        LOG.info("Get NDC Threshold Detail List Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdDetails") List<ThresholdDetail> thresholdDetails
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ThresholdDetail(
        @JsonProperty("id") Long id,
        @JsonProperty("thresholdConfigurationId") Long thresholdConfigurationId,
        @JsonProperty("participantCurrencyId") Long participantCurrencyId,
        @JsonProperty("dfspId") String dfspId,
        @JsonProperty("currency") String currency,
        @JsonProperty("visualConfig") BigDecimal visualConfig,
        @JsonProperty("ndcConfig") BigDecimal ndcConfig,
        @JsonProperty("status") Boolean status
    ) { }
}
