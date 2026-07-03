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
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateSettlementModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class CreateSettlementModelController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateSettlementModelController.class);

    private final CreateSettlementModel createSettlementModel;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/createSettlementModel")
    public ResponseEntity<Response> execute(
        @Valid @RequestBody Request request) throws DomainException, JsonProcessingException {

        LOG.info("Create Settlement Model Request : [{}]", this.objectMapper.writeValueAsString(request));

        CreateSettlementModel.Output output = this.createSettlementModel.execute(
            new CreateSettlementModel.Input(request.name(),
                                            request.modelType(),
                                            (request.currencyId()
                                                    .isEmpty() || request.currencyId()
                                                                         .isBlank()) ?
                                                null : request.currencyId(),
                                            request.zoneId(),
                                            request.requireLiquidityCheck(),
                                            request.autoPositionReset(),
                                            request.adjustPosition()));

        var response = new Response(output.created(),
                                    output.settlementModelId()
                                          .getEntityId()
                                          .toString());

        LOG.info("Create New Settlement Model Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @JsonProperty("name") String name,
                          @JsonProperty("modelType") String modelType,
                          @JsonProperty("currencyId") String currencyId,
                          @JsonProperty("zoneId") String zoneId,
                          @JsonProperty("requireLiquidityCheck") boolean requireLiquidityCheck,
                          @JsonProperty("autoPositionReset") boolean autoPositionReset,
                          @JsonProperty("adjustPosition") boolean adjustPosition) implements Serializable {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("isCreated") boolean isCreated,
                           @JsonProperty("settlementModelId") String settlementModelId) implements Serializable {
    }

}
