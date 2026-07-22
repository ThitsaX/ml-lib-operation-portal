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
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.RemoveThresholdDetail;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class RemoveThresholdDetailController {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveThresholdDetailController.class);

    private final RemoveThresholdDetail removeThresholdDetail;

    private final ObjectMapper objectMapper;

    @DeleteMapping("/secured/ndc/thresholdDetails")
    public ResponseEntity<Response> execute(@RequestParam("id") String id)
        throws DomainException, JsonProcessingException {

        LOG.info("Remove NDC Threshold Detail Request : id=[{}]", id);

        UserContext userContext =
            (UserContext) SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getDetails();

        RemoveThresholdDetail.Output output = this.removeThresholdDetail.execute(
            new RemoveThresholdDetail.Input(Long.parseLong(id), userContext.userId().toString()));

        var response = new Response(String.valueOf(output.thresholdDetailId().getEntityId()), output.removed());

        LOG.info("Remove NDC Threshold Detail Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdDetailId") String thresholdDetailId,
        @JsonProperty("removed") boolean removed
    ) implements Serializable { }
}
