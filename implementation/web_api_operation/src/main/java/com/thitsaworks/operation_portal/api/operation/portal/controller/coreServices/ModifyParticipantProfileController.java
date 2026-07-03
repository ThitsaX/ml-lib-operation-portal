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
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.type.Mobile;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyParticipantProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.Base64;

@RestController
@RequiredArgsConstructor
public class ModifyParticipantProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyParticipantProfileController.class);

    private final ModifyParticipantProfile modifyParticipantProfile;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/modifyParticipant")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify Participant Profile Request : [{}]", this.objectMapper.writeValueAsString(request));

        UserContext
            userContext =
            (UserContext) SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getDetails();

        ModifyParticipantProfile.Output output = this.modifyParticipantProfile.execute(
            new ModifyParticipantProfile.Input(new ParticipantId(Long.parseLong(request.participantId())),
                                               request.description(),
                                               request.address(),
                                               request.mobile() != null && !request.mobile()
                                                                                   .isBlank() ?
                                                   new Mobile(request.mobile()) : null,
                                               request.logoFileType(),
                                               Base64.getDecoder()
                                                     .decode(request.logoBase64())));

        var response = new Response(output.participantId()
                                          .getId()
                                          .toString(), output.modified());

        LOG.info("Modify Participant Profile Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @NotBlank @JsonProperty("participantId") String participantId,
                          @NotNull @JsonProperty("description") String description,
                          @NotNull @JsonProperty("address") String address,
                          @NotNull @JsonProperty("mobile") String mobile,
                          @NotNull @JsonProperty("logoFileType") String logoFileType,
                          @NotNull @JsonProperty("logo") String logoBase64
    ) implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("participantId") String participantId,
                           @JsonProperty("modified") boolean modified) implements Serializable { }

}
