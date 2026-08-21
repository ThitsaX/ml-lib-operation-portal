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
import com.thitsaworks.operation_portal.api.operation.portal.validation.PostParticipantBalanceRequestValidator;
import com.thitsaworks.operation_portal.component.fspiop.model.ExtensionList;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.SubmitParticipantBalance;
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
public class PostParticipantBalanceController {

    private static final Logger LOG = LoggerFactory.getLogger(PostParticipantBalanceController.class);

    private final SubmitParticipantBalance submitParticipantBalance;

    private final ObjectMapper objectMapper;

    @PostMapping(
        value = "/secured/postParticipantBalance",
        consumes = "application/json",
        produces = "application/json")
    public ResponseEntity<Response> execute(@RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Post Participant Balance Request : [{}]",
                 this.objectMapper.writeValueAsString(request));

        UserContext userContext = (UserContext) SecurityContextHolder.getContext()
                                                                       .getAuthentication()
                                                                       .getDetails();

        PostParticipantBalanceRequestValidator.Values values =
            PostParticipantBalanceRequestValidator.validate(request.participantId(),
                                                              request.action(),
                                                              request.amount(),
                                                              request.currency());

        SubmitParticipantBalance.Output output = this.submitParticipantBalance.execute(
            new SubmitParticipantBalance.Input(
                values.participantId(),
                values.action(),
                values.amount(),
                values.currency(),
                request.extensionList(),
                userContext.userId()));

        Response response = new Response(output.status());

        LOG.info("Post Participant Balance Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @JsonProperty("participantId")
        String participantId,

        @JsonProperty("action")
        String action,

        @JsonProperty("amount")
        String amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("extensionList")
        ExtensionList extensionList) implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("status")
        String status) implements Serializable { }

}
