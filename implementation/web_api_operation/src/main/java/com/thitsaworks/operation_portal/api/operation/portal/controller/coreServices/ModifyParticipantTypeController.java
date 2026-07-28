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
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyParticipantType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ModifyParticipantTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyParticipantTypeController.class);

    private final ModifyParticipantType modifyParticipantType;

    private final ObjectMapper objectMapper;

    @PutMapping(value = "/secured/modifyParticipantType")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify Participant Type Request : [{}]", this.objectMapper.writeValueAsString(request));

        List<ModifyParticipantType.ParticipantTypeInfo> participantTypeInfoList = new ArrayList<>();

        for (var participantTypeInfo : request.participantTypeInfoList()) {
            participantTypeInfoList.add(new ModifyParticipantType.ParticipantTypeInfo(
                participantTypeInfo.participantName(),
                participantTypeInfo.participantType()));
        }

        var output = this.modifyParticipantType.execute(new ModifyParticipantType.Input(participantTypeInfoList));

        List<Response.ParticipantTypeInfo> responseParticipantTypeInfoList = new ArrayList<>();

        for (var participantTypeInfo : output.participantTypeInfoList()) {
            responseParticipantTypeInfoList.add(new Response.ParticipantTypeInfo(
                participantTypeInfo.participantName(),
                participantTypeInfo.participantType()));
        }

        var response = new Response(responseParticipantTypeInfoList);

        LOG.info("Modify Participant Type Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull @NotEmpty @Valid @JsonProperty("participantInfoList")
        List<ParticipantTypeInfo> participantTypeInfoList) implements Serializable {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ParticipantTypeInfo(
            @NotNull @NotBlank @JsonProperty("participantName") String participantName,
            @JsonProperty("participantType") String participantType) implements Serializable { }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("participantInfoList") List<ParticipantTypeInfo> participantTypeInfoList)
        implements Serializable {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ParticipantTypeInfo(
            @JsonProperty("participantName") String participantName,
            @JsonProperty("participantType") String participantType) implements Serializable { }
    }

}
