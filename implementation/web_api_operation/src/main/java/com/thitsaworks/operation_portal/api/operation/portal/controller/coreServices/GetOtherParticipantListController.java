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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetOtherParticipantList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetOtherParticipantListController {

    private static final Logger LOG = LoggerFactory.getLogger(GetOtherParticipantListController.class);

    private final GetOtherParticipantList getOtherParticipantList;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getOtherParticipantList")
    public ResponseEntity<Response> execute(
        @RequestParam("participantId") String participantId) throws DomainException, JsonProcessingException {

        LOG.info("Get All Participants Request : participantId = [{}]", participantId);

        GetOtherParticipantList.Output output = this.getOtherParticipantList.execute(
            new GetOtherParticipantList.Input(new ParticipantId(Long.parseLong(participantId))));

        List<Response.ParticipantInfo> participantInfoList = new ArrayList<>();

        for (var participant : output.participantInfoList()) {
            participantInfoList.add(new Response.ParticipantInfo(participant.participantId()
                                                                            .getId()
                                                                            .toString(),
                                                                 participant.participantName().getValue(),
                                                                 participant.description()));
        }

        var response = new Response(participantInfoList);

        LOG.info("Get All Participants List Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("participantInfoList") List<ParticipantInfo> participantInfoList)
        implements Serializable {

        public record ParticipantInfo(@JsonProperty("participantId") String participantId,
                                      @JsonProperty("participantName") String participantName,
                                      @JsonProperty("description") String description
        ) implements Serializable { }

    }

}
