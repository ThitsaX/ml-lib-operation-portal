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
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.core.notification.data.NdcAlertEventData;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetNdcAlertEventList;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetNdcAlertEventListController {

    private final GetNdcAlertEventList getNdcAlertEventList;

    @GetMapping("/secured/ndc/getAlertEvents")
    public ResponseEntity<Response> execute(
        @RequestParam(value = "participantName", required = false) String participantName,
        @RequestParam(value = "dfspId", required = false) String dfspId,
        @RequestParam(value = "currency", required = false) String currency,
        @RequestParam(value = "currentState", required = false) NdcThresholdStateType currentState,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) throws DomainException {

        List<NdcAlertEventData> result = this.getNdcAlertEventList.execute(
            new GetNdcAlertEventList.Input(participantName,
                                           currency,
                                           currentState,
                                           from,
                                           to)).alerts();

        return new ResponseEntity<>(new Response(result), HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("alerts") List<NdcAlertEventData> alerts) {
    }
}
