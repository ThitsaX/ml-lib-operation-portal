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
package com.thitsaworks.operation_portal.api.operation.portal.controller.revenueServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.RevenuePartyId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenueParty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ModifyRevenuePartyController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyRevenuePartyController.class);

    private final ModifyRevenueParty modifyRevenueParty;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/modifyRevenueParty")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify Revenue Party Request : [{}]", this.objectMapper.writeValueAsString(request));

        var output = this.modifyRevenueParty.execute(new ModifyRevenueParty.Input(
            new RevenuePartyId(request.revenuePartyId()), request.partyCode(), request.partyName(),
            request.partyType(), request.description()));
        var response = new Response(output.modified(), output.revenuePartyId().getId().toString());

        LOG.info("Modify Revenue Party Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @JsonProperty("revenuePartyId") Long revenuePartyId,
                          @NotBlank @JsonProperty("partyCode") String partyCode,
                          @NotBlank @JsonProperty("partyName") String partyName,
                          @NotBlank @JsonProperty("partyType") String partyType,
                          @JsonProperty("description") String description)
        implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("modified") boolean modified,
                           @JsonProperty("revenuePartyId") String revenuePartyId)
        implements Serializable { }

}
