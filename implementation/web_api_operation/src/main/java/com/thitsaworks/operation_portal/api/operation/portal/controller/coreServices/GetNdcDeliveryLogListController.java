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
import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetNdcDeliveryLogList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetNdcDeliveryLogListController {

    private final GetNdcDeliveryLogList getNdcDeliveryLogList;

    @GetMapping("/secured/ndc/delivery-logs")
    public ResponseEntity<Response> execute(
        @RequestParam(value = "deliveryStatus", required = false) NdcDeliveryStatus deliveryStatus,
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize) throws DomainException {

        GetNdcDeliveryLogList.Output output = this.getNdcDeliveryLogList.execute(
            new GetNdcDeliveryLogList.Input(deliveryStatus, page, pageSize));

        List<NdcNotificationDispatch> deliveryLogs = new ArrayList<>();
        for (var deliveryLog : output.deliveryLogs()) {
            deliveryLogs.add(new NdcNotificationDispatch(
                deliveryLog.ndcNotificationDispatchLogId().getEntityId().toString(),
                deliveryLog.alertEventId().getEntityId().toString(),
                deliveryLog.participantName(),
                deliveryLog.currency(),
                deliveryLog.recipientType().name(),
                deliveryLog.recipientUserId(),
                deliveryLog.recipientName(),
                deliveryLog.recipientEmail(),
                deliveryLog.deliveryStatus(),
                deliveryLog.attemptNo(),
                deliveryLog.lastAttemptAt(),
                deliveryLog.sentAt(),
                deliveryLog.errorMessage(),
                deliveryLog.createdAt(),
                deliveryLog.updatedAt()));
        }

        return new ResponseEntity<>(new Response(deliveryLogs, output.total(), output.totalPages()), HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("deliveryLogs") List<NdcNotificationDispatch> deliveryLogs,
                           @JsonProperty("total") long total,
                           @JsonProperty("totalPages") Integer totalPages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NdcNotificationDispatch(
            @JsonProperty("ndcNotificationDispatchLogId") String ndcNotificationDispatchLogId,
            @JsonProperty("alertEventId") String alertEventId,
            @JsonProperty("participantName") String participantName,
            @JsonProperty("currency") String currency,
            @JsonProperty("recipientType") String recipientType,
            @JsonProperty("recipientUserId") String recipientUserId,
            @JsonProperty("recipientName") String recipientName,
            @JsonProperty("recipientEmail") String recipientEmail,
            @JsonProperty("deliveryStatus") NdcDeliveryStatus deliveryStatus,
            @JsonProperty("attemptNo") int attemptNo,
            @JsonProperty("lastAttemptAt") Instant lastAttemptAt,
            @JsonProperty("sentAt") Instant sentAt,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt
    ) {
    }
}
