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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetNdcSchemeThresholdConfiguration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetNdcSchemeThresholdConfigurationController {

    private static final Logger LOG = LoggerFactory.getLogger(GetNdcSchemeThresholdConfigurationController.class);

    private final GetNdcSchemeThresholdConfiguration getNdcSchemeThresholdConfiguration;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/ndc/configurations/scheme")
    public ResponseEntity<Response> execute()
        throws DomainException, JsonProcessingException {

        LOG.info("Get NDC Scheme Threshold Configuration Request");

        GetNdcSchemeThresholdConfiguration.Output output =
            this.getNdcSchemeThresholdConfiguration.execute(new GetNdcSchemeThresholdConfiguration.Input());

        var response = new Response(
                output.thresholdConfigurationId().toString(),
                output.scopeType().toString(),
                output.dfspId(),
                output.thresholdEnabled(),
                output.status().toString(),
                output.createdBy(),
                output.updatedBy()
        );

        LOG.info("Get NDC Scheme Threshold Configuration Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("thresholdConfigurationId") String thresholdConfigurationId,
            @JsonProperty("thresholdScopeType") String thresholdScopeType,
            @JsonProperty("dfspId") String dfspId,
            @JsonProperty("thresholdEnabled") Boolean thresholdEnabled,
            @JsonProperty("ndcConfigurationStatus") String ndcConfigurationStatus,
            @JsonProperty("createBy") String createdBy,
            @JsonProperty("updatedBy") String updatedBy
            ) { }

}
