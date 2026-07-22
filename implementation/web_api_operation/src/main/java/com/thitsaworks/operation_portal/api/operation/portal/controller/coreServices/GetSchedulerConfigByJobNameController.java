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
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSchedulerConfigByJobName;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetSchedulerConfigByJobNameController {

    private static final Logger LOG = LoggerFactory.getLogger(GetSchedulerConfigByJobNameController.class);

    private final GetSchedulerConfigByJobName getSchedulerConfigByJobName;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getSchedulerConfigByJobName")
    public ResponseEntity<Response> execute(@Valid @RequestParam("jobName")  String jobName)
        throws DomainException, JsonProcessingException {

        LOG.info("Get Scheduler Config by Job Name Request : jobName = [{}]", jobName);

        GetSchedulerConfigByJobName.Output output =
            this.getSchedulerConfigByJobName.execute(new GetSchedulerConfigByJobName.Input(jobName));

        var response = new Response(output.config());

        LOG.info("Get Scheduler Config by Job Name Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("config") SchedulerConfigData config
    ) {}
}
