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
package com.thitsaworks.operation_portal.api.operation.portal.controller.revenueServices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.TimeZoneOffsetFormater;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateRevenueSharingSummaryReport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class GenerateRevenueSharingSummaryReportController {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateRevenueSharingSummaryReportController.class);

    private final GenerateRevenueSharingSummaryReport generateRevenueSharingSummaryReport;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/generateRevenueSharingSummaryReport")
    public ResponseEntity<Response> execute(
        @RequestParam(value = "date", required = false, defaultValue = "") String date,
        @RequestParam(value = "settlementId", required = false, defaultValue = "")
        String settlementId,
        @RequestParam("fileType") String fileType,
        @RequestParam("timezoneOffset") String timezoneOffset)
        throws DomainException, JsonProcessingException {

        LOG.info(
            "Generate Revenue Sharing Summary Report : date = [{}], settlementId = [{}], fileType = [{}], timezoneOffset = [{}]",
            date, settlementId, fileType, timezoneOffset);

        String normalizedTimezoneOffset = TimeZoneOffsetFormater.normalizeOffsetFormat(timezoneOffset);
        UserContext userContext = (UserContext) SecurityContextHolder
                                                    .getContext()
                                                    .getAuthentication()
                                                    .getDetails();

        GenerateRevenueSharingSummaryReport.Output output =
            this.generateRevenueSharingSummaryReport.execute(
                new GenerateRevenueSharingSummaryReport.Input(
                    date, settlementId, normalizedTimezoneOffset, fileType, userContext.userId().getId()));

        var response = new Response(
            output.requestId().getEntityId().toString(),
            output.status().name(),
            output.fileUrl(),
            output.paramsSignature());

        LOG.info(
            "Generate Revenue Sharing Summary Report Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public record Response(@JsonProperty("requestId") String requestId,
                           @JsonProperty("status") String status,
                           @JsonProperty("fileUrl") String fileUrl,
                           @JsonProperty("paramsSignature") String paramsSignature)
        implements Serializable { }
}
