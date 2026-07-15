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
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenueConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class ModifyRevenueConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyRevenueConfigController.class);

    private final ModifyRevenueConfig modifyRevenueConfig;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/modifyRevenueConfig")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify Revenue Config Request : [{}]", this.objectMapper.writeValueAsString(request));

        ModifyRevenueConfig.Output output = this.modifyRevenueConfig.execute(
            new ModifyRevenueConfig.Input(new RevenueConfigId(request.id()),
                                                 request.taxCodeId(),
                                                 request.taxCodeDescription(),
                                                 request.category(),
                                                 request.responsibleMinistryId(),
                                                 request.thirdPartyProviderId(),
                                                 request.golPercentage(),
                                                 request.ministryPercentage(),
                                                 request.thirdPartyPercentage(),
                                                 request.sendingDfspPercentage()));

        Response response = new Response(output.modified());

        LOG.info("Modify Revenue Config Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @JsonProperty("id") Long id,
                          @NotBlank @JsonProperty("taxCodeId") String taxCodeId,
                          @NotBlank @JsonProperty("taxCodeDescription") String taxCodeDescription,
                          @NotNull @JsonProperty("category") RevenueConfigCategory category,
                          @NotNull @JsonProperty("responsibleMinistryId") Long responsibleMinistryId,
                          @JsonProperty("thirdPartyProviderId") Long thirdPartyProviderId,
                          @NotNull @JsonProperty("golPercentage") BigDecimal golPercentage,
                          @NotNull @JsonProperty("ministryPercentage") BigDecimal ministryPercentage,
                          @NotNull @JsonProperty("thirdPartyPercentage") BigDecimal thirdPartyPercentage,
                          @NotNull @JsonProperty("sendingDfspPercentage") BigDecimal sendingDfspPercentage) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("modified") boolean modified) { }
}
