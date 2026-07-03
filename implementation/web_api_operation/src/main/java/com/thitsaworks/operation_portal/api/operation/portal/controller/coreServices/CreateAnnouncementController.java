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
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateAnnouncement;
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
import java.text.ParseException;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class CreateAnnouncementController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAnnouncementController.class);

    private final CreateAnnouncement createAnnouncement;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/createAnnouncement")
    public ResponseEntity<Response> execute(
        @Valid @RequestBody CreateAnnouncementController.Request request)
        throws DomainException, ParseException, JsonProcessingException {

        LOG.info("Create Announcement Request : [{}]", this.objectMapper.writeValueAsString(request));

        CreateAnnouncement.Output output = this.createAnnouncement.execute(
            new CreateAnnouncement.Input(request.announcementTitle,
                                         request.announcementDetail,
                                         Instant.parse(request.announcementDate)));

        Response response = new Response(
            output.created());

        LOG.info("Create Announcement Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull @NotBlank
        @JsonProperty("announcementTitle")
        String announcementTitle,

        @NotNull @NotBlank
        @JsonProperty("announcementDetail")
        String announcementDetail,

        @NotNull @NotBlank
        @JsonProperty("announcementDate")
        String announcementDate
    ) implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("created")
        boolean created
    ) implements Serializable { }

}
