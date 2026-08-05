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
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyDfspThresholdConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ModifyDfspThresholdConfigurationController {

    private final ModifyDfspThresholdConfiguration modifyDfspThresholdConfiguration;

    @PutMapping("/secured/ndc/modifyDfspThresholdConfiguration")
    public ResponseEntity<Response> execute(@RequestParam("id") String id,
                                            @Valid @RequestBody Request request)
        throws DomainException {

        UserContext userContext =
            (UserContext) SecurityContextHolder.getContext()
                                         .getAuthentication()
                                         .getDetails();

        ModifyDfspThresholdConfiguration.Output output =
            this.modifyDfspThresholdConfiguration.execute(
                new ModifyDfspThresholdConfiguration.Input(
                    Long.parseLong(id),
                    request.thresholdEnabled(),
                    request.status(),
                    userContext.userId().toString()));

        return ResponseEntity.ok(new Response(
            String.valueOf(output.thresholdConfigurationId().getEntityId()),
            output.modified()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotNull @JsonProperty("thresholdEnabled") Boolean thresholdEnabled,
        @NotNull @JsonProperty("status") NdcConfigurationStatus status) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("thresholdConfigurationId") String thresholdConfigurationId,
        @JsonProperty("modified") Boolean modified) { }
}
