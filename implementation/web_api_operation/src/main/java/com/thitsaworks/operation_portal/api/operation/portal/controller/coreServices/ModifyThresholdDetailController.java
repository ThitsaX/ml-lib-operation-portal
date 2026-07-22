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
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyThresholdDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class ModifyThresholdDetailController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyThresholdDetailController.class);

    private final ModifyThresholdDetail modifyThresholdDetail;

    private final ObjectMapper objectMapper;

    @PutMapping("/secured/ndc/thresholdDetails")
    public ResponseEntity<Response> execute(@RequestParam("id") String id,
                                            @Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify NDC Threshold Detail Request : id=[{}], request=[{}]",
                 id, this.objectMapper.writeValueAsString(request));

        UserContext userContext =
            (UserContext) SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getDetails();

        ModifyThresholdDetail.Output output = this.modifyThresholdDetail.execute(
            new ModifyThresholdDetail.Input(
                Long.parseLong(id),
                request.currency(),
                request.visualConfig(),
                request.ndcConfig(),
                request.status(),
                userContext.userId().toString()));

        var response = new Response(String.valueOf(output.thresholdDetailId().getEntityId()), output.modified());

        LOG.info("Modify NDC Threshold Detail Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotBlank @JsonProperty("currency") String currency,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        @JsonProperty("visualConfig") BigDecimal visualConfig,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        @JsonProperty("ndcConfig") BigDecimal ndcConfig,
        @NotNull @JsonProperty("status") Boolean status
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdDetailId") String thresholdDetailId,
        @JsonProperty("modified") boolean modified
    ) implements Serializable { }
}
