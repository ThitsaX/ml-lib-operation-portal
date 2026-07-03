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
package com.thitsaworks.operation_portal.api.operation.portal.controller.hubServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateFeeSettlementReport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class GenerateFeeSettlementReportController {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateFeeSettlementReportController.class);

    private final GenerateFeeSettlementReport generateFeeSettlementReport;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/generateFeeReport")
    public ResponseEntity<Response> execute(
        @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,
        @RequestParam("fromFspId") String fromFspId, @RequestParam("toFspId") String toFspId,
        @RequestParam("currency") String currency,
        @RequestParam("timezoneOffset") String timezoneOffset,
        @RequestParam("fileType") String fileType) throws DomainException, JsonProcessingException {

        LOG.info("Generate Fee Report Request : startDate = [{}], endDate = [{}], fromFspId = [{}], " +
                     "toFspId = [{}], currency = [{}], timezoneOffset = [{}], fileType = [{}]",
                 startDate, endDate, fromFspId, toFspId, currency, timezoneOffset, fileType);

        GenerateFeeSettlementReport.Output output = this.generateFeeSettlementReport.execute(
            new GenerateFeeSettlementReport.Input(Instant.parse(startDate), Instant.parse(endDate), fromFspId,
                                                  toFspId,
                                                  currency,
                                                  timezoneOffset,
                                                  fileType));

        var response = new Response(output.rptData());

        LOG.info("Generate Fee Report Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("rptByte") byte[] feeReportByte) implements Serializable { }

}
