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
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateGreetingMessage;
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
import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class CreateGreetingMessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateGreetingMessageController.class);

    private final CreateGreetingMessage createGreetingMessage;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/createGreetingMessage")
    public ResponseEntity<Response> execute(
        @Valid @RequestBody Request request) throws DomainException, JsonProcessingException {

        LOGGER.info("Create Greeting Message Request : [{}]", this.objectMapper.writeValueAsString(request));

        var input = new CreateGreetingMessage.Input(
            request.greetingTitle(),
            request.greetingDetail(),
            Instant.parse(request.greetingDate()));

        var output = this.createGreetingMessage.execute(input);

        var response = new Response(output.created());

        LOGGER.info("Create Greeting Message Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull
        @JsonProperty("greetingTitle")
        String greetingTitle,

        @NotNull
        @JsonProperty("greetingDetail")
        String greetingDetail,

        @NotNull @NotBlank @JsonProperty("greetingDate")
        String greetingDate) implements Serializable {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("isCreated")
        boolean isCreated) implements Serializable {

    }

}
