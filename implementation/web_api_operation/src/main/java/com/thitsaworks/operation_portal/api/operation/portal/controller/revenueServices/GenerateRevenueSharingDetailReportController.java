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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.TimeZoneOffsetFormater;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateRevenueSharingDetailReport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class GenerateRevenueSharingDetailReportController {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateRevenueSharingDetailReportController.class);

    private final GenerateRevenueSharingDetailReport generateRevenueSharingDetailReport;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/generateRevenueSharingDetailReport")
    public ResponseEntity<Response> execute(@RequestParam("settlementId") String settlementId,
                                            @RequestParam("fileType") String fileType,
                                            @RequestParam("timezoneOffset") String timezoneOffset,
                                            @RequestParam("taxCode") String taxCode,
                                            @RequestParam("category") String category)
        throws DomainException, JsonProcessingException {

        LOG.info(
            "Generate Revenue Sharing Detail Report Request : settlementId = [{}], fileType = [{}], timezoneOffset = [{}], taxCode = [{}], category = [{}]",
            settlementId, fileType, timezoneOffset, taxCode, category);

        var timezone = TimeZoneOffsetFormater.normalizeOffset(timezoneOffset);

        GenerateRevenueSharingDetailReport.Output output = this.generateRevenueSharingDetailReport.execute(
            new GenerateRevenueSharingDetailReport.Input(settlementId, fileType, timezone, taxCode, category));

        var response = new Response(
            output.requestId().getEntityId().toString(), output.status().name(), output.fileUrl(),
            output.paramsSignature());

        LOG.info(
            "Generate Revenue Sharing Detail Report Response : [{}]",
            this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("requestId") String requestId,
                           @JsonProperty("status") String status,
                           @JsonProperty("fileUrl") String fileUrl,
                           @JsonProperty("paramsSignature") String paramsSignature)
        implements Serializable { }

}
