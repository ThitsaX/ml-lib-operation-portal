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
import com.thitsaworks.operation_portal.component.common.identifier.ContactId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.type.ContactType;
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.component.common.type.Mobile;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyContact;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class ModifyContactController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyContactController.class);

    private final ModifyContact modifyContact;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/modifyContact")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify Contact Request : [{}]", this.objectMapper.writeValueAsString(request));

        ModifyContact.Output output = this.modifyContact.execute(
            new ModifyContact.Input(new ParticipantId(Long.parseLong(request.participantId())),
                                    new ContactId(Long.parseLong(request.contactId())),
                                    request.name(),
                                    request.position(),
                                    request.email() != null ? new Email(request.email()) : null,
                                    request.mobile() != null ? new Mobile(request.mobile()) : null,
                                    ContactType.valueOf(request.contactType()
                                                               .toUpperCase())));

        var response = new Response(output.modified());

        LOG.info("Modify Contact Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @NotBlank @JsonProperty("participantId") String participantId,
                          @NotNull @NotBlank @JsonProperty("contactId") String contactId,
                          @NotNull @JsonProperty("name") String name,
                          @NotNull @JsonProperty("position") String position,
                          @NotNull @JsonProperty("email") String email,
                          @NotNull @JsonProperty("mobile") String mobile,
                          @NotNull @NotBlank @JsonProperty("contactType") String contactType)
        implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("modified") boolean modified) implements Serializable {

    }

}
