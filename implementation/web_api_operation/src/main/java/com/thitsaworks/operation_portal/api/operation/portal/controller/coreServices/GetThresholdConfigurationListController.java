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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetThresholdConfigurationList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetThresholdConfigurationListController {

    private static final Logger LOG = LoggerFactory.getLogger(GetThresholdConfigurationListController.class);

    private final GetThresholdConfigurationList getThresholdConfigurationList;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/ndc/getThresholdConfigurationList")
    public ResponseEntity<Response> execute() throws DomainException, JsonProcessingException {

        LOG.info("Get NDC Threshold Configuration List Request");

        GetThresholdConfigurationList.Output output =
            this.getThresholdConfigurationList.execute(new GetThresholdConfigurationList.Input());

        List<NdcThresholdConfiguration> configurations = output.configurations().stream()
            .map(config -> new NdcThresholdConfiguration(
                String.valueOf(config.thresholdConfigurationId().getEntityId()),
                config.scopeType().toString(),
                config.dfspId(),
                config.thresholdEnabled(),
                config.status().toString(),
                config.createdAt(),
                config.createdBy(),
                config.updatedAt(),
                config.updatedBy()
            ))
            .toList();
        var response = new Response(configurations);

        LOG.info("Get NDC Threshold Configuration List Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("configurations") List<NdcThresholdConfiguration> configurations
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NdcThresholdConfiguration(
            @JsonProperty("thresholdConfigurationId") String thresholdConfigurationId,
            @JsonProperty("thresholdScopeType") String thresholdScopeType,
            @JsonProperty("dfspId") String dfspId,
            @JsonProperty("thresholdEnabled") Boolean thresholdEnabled,
            @JsonProperty("ndcConfigurationStatus") String ndcConfigurationStatus,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("updatedAt") Instant updatedAt,
            @JsonProperty("updatedBy") String updatedBy
    ) { }

}
