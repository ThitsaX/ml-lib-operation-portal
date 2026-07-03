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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSettlementId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetSettlementIdController {

    private static final Logger LOG = LoggerFactory.getLogger(GetSettlementIdController.class);

    private final GetSettlementId getSettlementId;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getSettlementId")
    public ResponseEntity<Response> execute(@RequestParam("startDate") String startDate,
                                            @RequestParam("endDate") String endDate,
                                            @RequestParam(
                                                value = "dfspId",
                                                required = false) String dfspId,
                                            @RequestParam("timezoneOffset") String timezoneOffset)
        throws DomainException, JsonProcessingException {

        LOG.info("Get SettlementId Request : startDate = [{}], endDate = [{}], dfspId = [{}], timezoneOffset = [{}]",
                 startDate, endDate, dfspId, timezoneOffset);

        GetSettlementId.Output output = this.getSettlementId.execute(
            new GetSettlementId.Input(Instant.parse(startDate),
                                      Instant.parse(endDate),
                                      (dfspId == null || dfspId.isBlank())
                                          ? null
                                          : Integer.parseInt(dfspId),
                                      timezoneOffset));

        List<SettlementIdInfo>
            settlementIdInfoList = output.settlementIds()
                                         .stream()
                                         .map(idType -> new SettlementIdInfo(idType.getSettlementId()))
                                         .sorted(
                                             Comparator.comparingInt(
                                                           d -> Integer.parseInt(((SettlementIdInfo) d).settlementId()))
                                                       .reversed())
                                         .toList();

        var response = new Response(settlementIdInfoList);

        LOG.info("Get SettlementId Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("settlementIdDataList") List<SettlementIdInfo> settlementIdDataList
    ) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SettlementIdInfo(
        @JsonProperty("settlementId") String settlementId
    ) {

    }

}
