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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.fspiop.model.ErrorInformation;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.CloseSettlementWindows;
import jakarta.validation.Valid;
import lombok.Getter;
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

@RestController
@RequiredArgsConstructor
public class CloseSettlementWindowsController {

    private static final Logger LOG = LoggerFactory.getLogger(CloseSettlementWindowsController.class);

    private final CloseSettlementWindows closeSettlementWindows;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/closeSettlementWindow")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Close Settlement Window Request : [{}]", this.objectMapper.writeValueAsString(request));

        UserContext userContext =
            (UserContext) SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getDetails();
        var
            output =
            this.closeSettlementWindows.execute(new CloseSettlementWindows.Input(request.state,
                                                                                 request.reason,
                                                                                 request.settlementWindowId));

        var response = new Response(output.getSettlementWindowId(),
                                    output.getState(),
                                    output.getReason(),
                                    output.getCreatedDate(),
                                    output.getClosedDate(),
                                    output.getChangedDate(),
                                    output.getErrorInformation());

        LOG.info("Close Settlement Window Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@JsonProperty("state") String state,
                          @JsonProperty("reason") String reason,
                          @JsonProperty("settlementWindowId") int settlementWindowId
    ) implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Response(@JsonProperty("settlementWindowId") int settlementWindowId,
                           @JsonProperty("state") String state,
                           @JsonProperty("reason") String reason,
                           @JsonProperty("createdDate") String createdDate,
                           @JsonProperty("closedDate") String closedDate,
                           @JsonProperty("changedDate") String changedDate,
                           @JsonProperty("errorInformation") ErrorInformation errorInformation
    ) implements Serializable { }

}