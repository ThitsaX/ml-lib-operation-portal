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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetPendingApprovalList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetPendingApprovalListController {

    private static final Logger LOG = LoggerFactory.getLogger(
        GetPendingApprovalListController.class);

    private final GetPendingApprovalList getPendingApprovalList;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getPendingApprovalList")
    public ResponseEntity<Response> execute() throws DomainException, JsonProcessingException {

        var output = this.getPendingApprovalList.execute(new GetPendingApprovalList.Input());

        var response = new Response(
            output
                .pendingApprovalList()
                .stream()
                .sorted(
                    Comparator.comparing(request -> request.requestedDateTime().getEpochSecond(),
                        Comparator.reverseOrder()))
                .map(request -> new Response.PendingApproval(
                    request.approvalRequestId().getEntityId().toString(), request.requestedAction(),
                    request.participantName(), request.currency(), request.amount(),
                    request.requestedBy(), request.requestedDateTime().getEpochSecond(),
                    request.respondedBy(), request.respondedDateTime() == null ? null :
                                               request.respondedDateTime().getEpochSecond(),
                    request.action().name(), request.requestCategory(),
                    request.details().stream().map(detail -> new Response.PendingApprovalDetail(
                        detail.tabCode(), detail.fieldKey(), detail.fieldLabel(),
                        detail.fieldValue(), detail.beforeValue(), detail.afterValue(),
                        detail.valueType(), detail.displayOrder())).toList()))
                .toList());

        LOG.info(
            "Get Pending Approval List Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("pendingApprovalList") List<PendingApproval> pendingApprovalList)
        implements Serializable {

        public record PendingApproval(@JsonProperty("approvalRequestId") String approvalRequestId,
                                      @JsonProperty("requestedAction") String requestedAction,
                                      @JsonProperty("participantName") String participantName,
                                      @JsonProperty("currency") String currency,
                                      @JsonProperty("amount") BigDecimal amount,
                                      @JsonProperty("requestedBy") String requestedBy,
                                      @JsonProperty("requestedDateTime") long requestedDateTime,
                                      @JsonProperty("respondedBy") String respondedBy,
                                      @JsonProperty("respondedDateTime") Long respondedDateTime,
                                      @JsonProperty("action") String action,
                                      @JsonProperty("requestCategory") String requestCategory,
                                      @JsonProperty("details") List<PendingApprovalDetail> details)
            implements Serializable { }

        public record PendingApprovalDetail(@JsonProperty("tabCode") String tabCode,
                                            @JsonProperty("fieldKey") String fieldKey,
                                            @JsonProperty("fieldLabel") String fieldLabel,
                                            @JsonProperty("fieldValue") String fieldValue,
                                            @JsonProperty("beforeValue") String beforeValue,
                                            @JsonProperty("afterValue") String afterValue,
                                            @JsonProperty("valueType") String valueType,
                                            @JsonProperty("displayOrder") Integer displayOrder)
            implements Serializable { }

    }

}
