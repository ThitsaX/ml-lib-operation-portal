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
import com.thitsaworks.operation_portal.component.common.identifier.AuditId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetAuditDetailById;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class GetAuditDetailByIdController {

    private static final Logger LOG = LoggerFactory.getLogger(GetAuditDetailByIdController.class);

    private final GetAuditDetailById getAuditDetailById;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getAuditDetailById")
    public ResponseEntity<Response> execute(@RequestParam("auditId") String auditId)
        throws DomainException, JsonProcessingException {

        LOG.info("Get Audit Detail By Id Request : auditId = [{}]", auditId);

        GetAuditDetailById.Output output = this.getAuditDetailById.execute(
            new GetAuditDetailById.Input(new AuditId(Long.parseLong(auditId))));

        var response = new Response(output.auditId()
                                          .getEntityId()
                                          .toString(),
                                    output.inputInfo(),
                                    output.outputInfo(),
                                    output.exceptionInfo());

        LOG.info("Get Audit Detail By Id Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("auditId") String auditId,
                           @JsonProperty("inputInfo") String inputInfo,
                           @JsonProperty("outputInfo") String outputInfo,
                           @JsonProperty("exceptionInfo") String exceptionInfo) implements Serializable { }

}
