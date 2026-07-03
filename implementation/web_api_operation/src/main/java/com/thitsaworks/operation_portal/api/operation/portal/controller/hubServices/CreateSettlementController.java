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
package com.thitsaworks.operation_portal.api.operation.portal.controller.hubServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementParticipant;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementWindow;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementWindowId;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateSettlement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CreateSettlementController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateSettlementController.class);

    private final CreateSettlement createSettlement;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/createSettlement")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Create Settlement Request : [{}]", this.objectMapper.writeValueAsString(request));

        var output = this.createSettlement.execute(new CreateSettlement.Input(request.settlementModel,
                                                                              request.reason,
                                                                              request.settlementWindowIdList));

        var response = new Response(output.settlementId(),
                                    output.state(),
                                    output.settlementWindowList(),
                                    output.settlementParticipantList());

        LOG.info("Create Settlement Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @JsonProperty("settlementModel") String settlementModel,
        @JsonProperty("reason") String reason,
        @JsonProperty("settlementWindowIdList") List<SettlementWindowId> settlementWindowIdList
    ) implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("settlementId") Integer settlementId,
        @JsonProperty("state") String state,
        @JsonProperty("settlementWindowList") List<SettlementWindow> settlementWindowList,
        @JsonProperty("participantList") List<SettlementParticipant> participantList
    ) implements Serializable { }

}