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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateRevenueParty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class CreateRevenuePartyController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateRevenuePartyController.class);

    private final CreateRevenueParty createRevenueParty;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/createRevenueParty")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request) throws Exception {

        LOG.info(
            "Create Revenue Party Request : [{}]", this.objectMapper.writeValueAsString(request));

        CreateRevenueParty.Output output = this.createRevenueParty.execute(
            new CreateRevenueParty.Input(request.partyCode(), request.partyName(),
                request.partyType(), request.description()));

        Response response = new Response(
            output.created(), output.revenuePartyId().getId().toString());

        LOG.info(
            "Create Revenue Party Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotBlank @JsonProperty("partyCode") String partyCode,
                          @NotBlank @JsonProperty("partyName") String partyName,
                          @NotBlank @JsonProperty("partyType") String partyType,
                          @JsonProperty("description") String description) implements Serializable {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("created") boolean created,
                           @JsonProperty("revenuePartyId") String revenuePartyId)
        implements Serializable {

    }

}
