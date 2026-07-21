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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetDfspThresholdConfiguration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class GetDfspThresholdConfigurationController {

    private static final Logger LOG = LoggerFactory.getLogger(GetDfspThresholdConfigurationController.class);

    private final GetDfspThresholdConfiguration getDfspThresholdConfiguration;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/ndc/configurations/participant-currency/{participantCurrencyId}")
    public ResponseEntity<Response> execute(@PathVariable("participantCurrencyId") Long participantCurrencyId)
        throws DomainException, JsonProcessingException {

        LOG.info("Get NDC DFSP Threshold Configuration Request : participantCurrencyId=[{}]",
                 participantCurrencyId);

        GetDfspThresholdConfiguration.Output output =
            this.getDfspThresholdConfiguration.execute(
                new GetDfspThresholdConfiguration.Input(participantCurrencyId));

        var response = new Response(
                output.thresholdConfigurationId().getEntityId(),
                output.scopeType().toString(),
                output.participantCurrencyId(),
                output.thresholdEnabled(),
                output.colorCode(),
                output.visualAlertPercent(),
                output.notiAlertPercent(),
                output.status().toString(),
                output.createdBy(),
                output.updatedBy()
        );

        LOG.info("Get NDC DFSP Threshold Configuration Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(

            @JsonProperty("thresholdConfigurationId") Long thresholdConfigurationId,
            @JsonProperty("thresholdScopeType") String thresholdScopeType,
            @JsonProperty("participantCurrencyId") Long participantCurrencyId,
            @JsonProperty("thresholdEnabled") Boolean thresholdEnabled,
            @JsonProperty("colorCode") String colorCode,
            @JsonProperty("visualAlertPercent") BigDecimal visualAlertPercent,
            @JsonProperty("notiAlertPercent") BigDecimal notiAlertPercent,
            @JsonProperty("ndcConfigurationStatus") String ndcConfigurationStatus,
            @JsonProperty("createBy") String createdBy,
            @JsonProperty("updatedBy") String updatedBy
    ) { }

}
