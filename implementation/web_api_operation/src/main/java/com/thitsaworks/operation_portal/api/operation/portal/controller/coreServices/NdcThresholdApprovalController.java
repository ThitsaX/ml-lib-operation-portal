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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdApprovalOperation;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetNdcThresholdApprovalList;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyNdcThresholdApprovalAction;
import com.thitsaworks.operation_portal.usecase.operation_portal.SubmitNdcThresholdApproval;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NdcThresholdApprovalController {

    private static final Logger LOG = LoggerFactory.getLogger(
        NdcThresholdApprovalController.class);

    private final SubmitNdcThresholdApproval submitNdcThresholdApproval;

    private final GetNdcThresholdApprovalList getNdcThresholdApprovalList;

    private final ModifyNdcThresholdApprovalAction modifyNdcThresholdApprovalAction;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/ndc/threshold-approvals")
    public ResponseEntity<SubmitResponse> submit(
        @Valid @RequestBody SubmitRequest request)
        throws DomainException, JsonProcessingException {

        LOG.info("Submit NDC threshold approval request: [{}]",
                 this.objectMapper.writeValueAsString(request));

        UserContext userContext = getUserContext();

        var output = this.submitNdcThresholdApproval.execute(
            new SubmitNdcThresholdApproval.Input(
                request.operation(),
                request.participantName(),
                request.thresholdDetailId(),
                request.currency(),
                request.visualConfig(),
                request.notificationConfig(),
                userContext.userId()));

        return new ResponseEntity<>(
            new SubmitResponse(
                output.approvalRequestId().getEntityId().toString(),
                output.status()),
            HttpStatus.CREATED);
    }

    @GetMapping("/secured/ndc/threshold-approvals")
    public ResponseEntity<ListResponse> get(
        @RequestParam(value = "status", defaultValue = "PENDING")
        ApprovalActionType status) throws DomainException {

        var output = this.getNdcThresholdApprovalList.execute(
            new GetNdcThresholdApprovalList.Input(status));

        List<ApprovalResponse> approvals = output.approvals()
                                                 .stream()
                                                 .map(ApprovalResponse::new)
                                                 .toList();

        return ResponseEntity.ok(new ListResponse(approvals));
    }

    @PutMapping("/secured/ndc/threshold-approvals/{approvalRequestId}/decision")
    public ResponseEntity<DecisionResponse> decide(
        @PathVariable("approvalRequestId") String approvalRequestId,
        @Valid @RequestBody DecisionRequest request)
        throws DomainException, JsonProcessingException {

        LOG.info("Modify NDC threshold approval request: id=[{}], request=[{}]",
                 approvalRequestId,
                 this.objectMapper.writeValueAsString(request));

        UserContext userContext = getUserContext();

        var output = this.modifyNdcThresholdApprovalAction.execute(
            new ModifyNdcThresholdApprovalAction.Input(
                new ApprovalRequestId(Long.parseLong(approvalRequestId)),
                request.action(),
                userContext.userId()));

        return ResponseEntity.ok(
            new DecisionResponse(
                output.approvalRequestId().getEntityId().toString(),
                output.status()));
    }

    private static UserContext getUserContext() {

        return (UserContext) SecurityContextHolder.getContext()
                                                  .getAuthentication()
                                                  .getDetails();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubmitRequest(
        @NotNull @JsonProperty("operation")
        NdcThresholdApprovalOperation operation,
        @NotBlank @JsonProperty("participantName")
        String participantName,
        @JsonProperty("thresholdDetailId")
        Long thresholdDetailId,
        @JsonProperty("currency")
        String currency,
        @DecimalMin("0.0") @DecimalMax("100.0")
        @JsonProperty("visualConfig")
        BigDecimal visualConfig,
        @DecimalMin("0.0") @DecimalMax("100.0")
        @JsonProperty("notificationConfig") @JsonAlias("ndcConfig")
        BigDecimal notificationConfig
    ) { }

    public record SubmitResponse(
        @JsonProperty("approvalRequestId") String approvalRequestId,
        @JsonProperty("status") String status
    ) { }

    public record ListResponse(
        @JsonProperty("approvals") List<ApprovalResponse> approvals
    ) { }

    public record ApprovalResponse(
        @JsonProperty("approvalRequestId") String approvalRequestId,
        @JsonProperty("operation") String operation,
        @JsonProperty("participantName") String participantName,
        @JsonProperty("currency") String currency,
        @JsonProperty("thresholdDetailId") String thresholdDetailId,
        @JsonProperty("previousVisualConfig") BigDecimal previousVisualConfig,
        @JsonProperty("requestedVisualConfig") BigDecimal requestedVisualConfig,
        @JsonProperty("previousNotificationConfig") BigDecimal previousNotificationConfig,
        @JsonProperty("requestedNotificationConfig") BigDecimal requestedNotificationConfig,
        @JsonProperty("requestedBy") String requestedBy,
        @JsonProperty("requestedAt") Instant requestedAt,
        @JsonProperty("respondedBy") String respondedBy,
        @JsonProperty("respondedAt") Instant respondedAt,
        @JsonProperty("status") String status
    ) {

        public ApprovalResponse(GetNdcThresholdApprovalList.Output.Approval approval) {

            this(
                approval.approvalRequestId(),
                approval.operation(),
                approval.participantName(),
                approval.currency(),
                approval.thresholdDetailId(),
                approval.previousVisualConfig(),
                approval.requestedVisualConfig(),
                approval.previousNotificationConfig(),
                approval.requestedNotificationConfig(),
                approval.requestedBy(),
                approval.requestedAt(),
                approval.respondedBy(),
                approval.respondedAt(),
                approval.status());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DecisionRequest(
        @NotNull @JsonProperty("action") ApprovalActionType action
    ) { }

    public record DecisionResponse(
        @JsonProperty("approvalRequestId") String approvalRequestId,
        @JsonProperty("status") String status
    ) { }
}
