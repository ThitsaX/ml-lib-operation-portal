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
import com.thitsaworks.operation_portal.api.operation.portal.security.NdcWorkerConfigRequestValidator;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateSchedulerConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
public class CreateNdcWorkerConfigController {

    private final CreateSchedulerConfig createSchedulerConfig;

    @PostMapping("/secured/ndc/worker-config")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request) throws DomainException {

        NdcWorkerConfigRequestValidator.Values values = NdcWorkerConfigRequestValidator.validate(request.runEvery());

        CreateSchedulerConfig.Output output = this.createSchedulerConfig.execute(
            new CreateSchedulerConfig.Input(request.name(),
                                            request.jobName(),
                                            request.description(),
                                            values.cronExpression(),
                                            ZoneId.of(request.zoneId())));

        return new ResponseEntity<>(new Response(output.created()), HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotBlank @JsonProperty("name") String name,
                          @NotBlank @JsonProperty("jobName") String jobName,
                          @NotBlank @JsonProperty("description") String description,
                          @NotBlank @JsonProperty("runEvery") String runEvery,
                          @NotBlank @JsonProperty("zoneId") String zoneId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("created") boolean created) {
    }
}
