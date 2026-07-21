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
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateRevenueApprovalRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CreateRevenueApprovalRequestController {

    private static final Logger LOG = LoggerFactory.getLogger(
        CreateRevenueApprovalRequestController.class);

    private final CreateRevenueApprovalRequest createRevenueApprovalRequest;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/createRevenueApprovalRequest")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info(
            "Create Revenue Approval Request : [{}]",
            this.objectMapper.writeValueAsString(request));

        UserContext userContext = (UserContext) SecurityContextHolder
                                                    .getContext()
                                                    .getAuthentication()
                                                    .getDetails();

        var output = this.createRevenueApprovalRequest.execute(
            new CreateRevenueApprovalRequest.Input(
                request.requestedAction(), request.taxCodeId(), request.taxCodeDescription(),
                RevenueConfigCategory.valueOf(request.category()), request.responsibleMinistryId(),
                request.thirdPartyProviderId(), request.startDate(), request.percentages(),
                userContext.userId()));

        var response = new Response(output.approvalRequestId().getEntityId().toString());

        LOG.info(
            "Create Revenue Approval Request Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@JsonProperty("requestedAction") String requestedAction,
                          @NotBlank @JsonProperty("taxCodeId") String taxCodeId,
                          @NotBlank @JsonProperty("taxCodeDescription") String taxCodeDescription,
                          @NotBlank @JsonProperty("category") String category,
                          @NotBlank @JsonProperty("responsibleMinistryId") String responsibleMinistryId,
                          @JsonProperty("thirdPartyProviderId") String thirdPartyProviderId,
                          @JsonProperty("startDate") String startDate,
                          @NotEmpty @JsonProperty("percentages") Map<@NotBlank String, @NotNull BigDecimal> percentages)
        implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("approvalRequestId") String approvalRequestId)
        implements Serializable { }

}
