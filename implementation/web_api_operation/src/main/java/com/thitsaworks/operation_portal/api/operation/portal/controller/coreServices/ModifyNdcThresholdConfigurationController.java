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
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyNdcThresholdConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class ModifyNdcThresholdConfigurationController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyNdcThresholdConfigurationController.class);

    private final ModifyNdcThresholdConfiguration modifyNdcThresholdConfiguration;

    private final ObjectMapper objectMapper;

    @PutMapping("/secured/ndc/configurations/{id}")
    public ResponseEntity<Response> execute(@PathVariable("id") String id,
                                            @Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify NDC Threshold Configuration Request : id=[{}], request=[{}]",
                 id, this.objectMapper.writeValueAsString(request));

        ModifyNdcThresholdConfiguration.Output output = this.modifyNdcThresholdConfiguration.execute(
            new ModifyNdcThresholdConfiguration.Input(
                id,
                request.thresholdEnabled(),
                request.status(),
                request.updatedBy()));

        var response = new Response(output.thresholdConfigurationId().toString(), output.modified());

        LOG.info("Modify NDC Threshold Configuration Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull @JsonProperty("thresholdEnabled") Boolean thresholdEnabled,
        @NotNull @JsonProperty("status") NdcConfigurationStatus status,
        @NotBlank @JsonProperty("updatedBy") String updatedBy
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("thresholdConfigurationId") String thresholdConfigurationId,
            @JsonProperty("modified") boolean modified) implements Serializable { }


}
