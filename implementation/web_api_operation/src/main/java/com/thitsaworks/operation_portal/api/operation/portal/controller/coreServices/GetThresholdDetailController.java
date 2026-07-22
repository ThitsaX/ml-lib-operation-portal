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
public class GetThresholdDetailController {

    private static final Logger LOG = LoggerFactory.getLogger(GetThresholdDetailController.class);

    private final GetThresholdDetail getThresholdDetail;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/ndc/thresholdDetails", params = "id")
    public ResponseEntity<Response> execute(@RequestParam("id") String id)
        throws DomainException, JsonProcessingException {

        LOG.info("Get NDC Threshold Detail Request : id=[{}]", id);

        GetThresholdDetail.Output output =
            this.getThresholdDetail.execute(new GetThresholdDetail.Input(Long.parseLong(id)));

        var response = new Response(new ThresholdDetail(
            String.valueOf(output.thresholdDetail().thresholdDetailId()),
            String.valueOf(output.thresholdDetail().thresholdConfigurationId()),
            output.thresholdDetail().currency(),
            output.thresholdDetail().visualConfig(),
            output.thresholdDetail().ndcConfig(),
            output.thresholdDetail().status()
        ));

        LOG.info("Get NDC Threshold Detail Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdDetail") ThresholdDetail thresholdDetail
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ThresholdDetail(
        @JsonProperty("thresholdDetailId") String thresholdDetailId,
        @JsonProperty("thresholdConfigurationId") String thresholdConfigurationId,
        @JsonProperty("currency") String currency,
        @JsonProperty("visualConfig") BigDecimal visualConfig,
        @JsonProperty("ndcConfig") BigDecimal ndcConfig,
        @JsonProperty("status") Boolean status
    ) { }
}
