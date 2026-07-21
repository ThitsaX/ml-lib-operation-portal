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
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateThresholdDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class CreateThresholdDetailController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateThresholdDetailController.class);

    private final CreateThresholdDetail createThresholdDetail;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/ndc/thresholdDetails")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Create NDC Threshold Detail Request : [{}]", this.objectMapper.writeValueAsString(request));

        UserContext userContext =
            (UserContext) SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getDetails();

        CreateThresholdDetail.Output output = this.createThresholdDetail.execute(
            new CreateThresholdDetail.Input(
                request.thresholdConfigurationId(),
                request.participantCurrencyId(),
                request.dfspId(),
                request.currency(),
                request.visualConfig(),
                request.ndcConfig(),
                userContext.userId().toString()));

        var response = new Response(output.thresholdDetailId().getEntityId());

        LOG.info("Create NDC Threshold Detail Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull @JsonProperty("thresholdConfigurationId") Long thresholdConfigurationId,
        @NotNull @JsonProperty("participantCurrencyId") Long participantCurrencyId,
        @NotBlank @JsonProperty("dfspId") String dfspId,
        @NotBlank @JsonProperty("currency") String currency,
        @NotNull  @JsonProperty("visualConfig") BigDecimal visualConfig,
        @NotNull @JsonProperty("ndcConfig") BigDecimal ndcConfig
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdDetailId") Long thresholdDetailId
    ) { }
}

