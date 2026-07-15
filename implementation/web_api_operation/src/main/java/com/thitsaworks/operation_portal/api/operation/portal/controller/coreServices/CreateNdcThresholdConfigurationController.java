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
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateNdcThresholdConfiguration;
import jakarta.validation.Valid;
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

@RestController
@RequiredArgsConstructor
public class CreateNdcThresholdConfigurationController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateNdcThresholdConfigurationController.class);

    private final CreateNdcThresholdConfiguration createNdcThresholdConfiguration;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/ndc/configurations")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Create NDC Threshold Configuration Request : [{}]", this.objectMapper.writeValueAsString(request));


        UserContext userContext =
                (UserContext) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getDetails();
        CreateNdcThresholdConfiguration.Output output = this.createNdcThresholdConfiguration.execute(
            new CreateNdcThresholdConfiguration.Input(
                request.scopeType(),
                request.dfspId(),
                request.thresholdEnabled(),
                userContext.userId().toString()));

        var response = new Response(output.thresholdConfigurationId().toString());

        LOG.info("Create NDC Threshold Configuration Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull @JsonProperty("scopeType") ThresholdScopeType scopeType,
        @JsonProperty("dfspId") String dfspId,
        @NotNull @JsonProperty("thresholdEnabled") Boolean thresholdEnabled,
        @NotBlank @JsonProperty("createdBy") String createdBy
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdConfigurationId") String thresholdConfigurationId
    ) { }

}
