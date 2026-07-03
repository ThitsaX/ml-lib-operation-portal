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
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetParticipantProfile;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetParticipantProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(
        GetParticipantProfileController.class);

    private final GetParticipantProfile getParticipantProfile;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getParticipantProfile")
    public ResponseEntity<Response> execute(@RequestParam("participantId") String participantId)
        throws DomainException, JsonProcessingException {

        LOG.info("Get Participant Profile Request : ParticipantId = [{}]", participantId);

        var output = this.getParticipantProfile.execute(
            new GetParticipantProfile.Input(new ParticipantId(Long.parseLong(participantId))));

        var response = new Response(
            output.participantId().getEntityId().toString(), output.participantName(),
            output.description(), output.address(),
            output.mobile() != null ? output.mobile().getValue() : null, output.logoFileType(),
            output.logoBase64() == null ? null :
                Base64.getEncoder().encodeToString(output.logoBase64()), output.connectionType(),
            output.connectedParticipants() == null ? List.of() : output
                                                                     .connectedParticipants()
                                                                     .stream()
                                                                     .map(
                                                                         participant -> new ConnectedParticipant(
                                                                             participant.participantName(),
                                                                             participant.participantDescription()))
                                                                     .toList(),
            output.createdDate().getEpochSecond());

        LOG.info(
            "Get Participant Profile Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("participantId") String participantId,
                           @JsonProperty("participantName") String participantName,
                           @JsonProperty("description") String description,
                           @JsonProperty("address") String address,
                           @JsonProperty("mobile") String mobile,
                           @JsonProperty("logoFileType") String logoFileType,
                           @JsonProperty("logo") String logoBase64,
                           @JsonProperty("connectionType") String connectionType,
                           @JsonProperty("connectedParticipants") List<ConnectedParticipant> connectedParticipants,
                           @JsonProperty("createdDate") long createdDate) implements Serializable {

        public Response {

            connectedParticipants =
                connectedParticipants != null ? connectedParticipants : List.of();

        }

    }

    public record ConnectedParticipant(@JsonProperty("participantName") String participantName,
                                       @JsonProperty("participantDescription") String participantDescription)
        implements Serializable { }

}
