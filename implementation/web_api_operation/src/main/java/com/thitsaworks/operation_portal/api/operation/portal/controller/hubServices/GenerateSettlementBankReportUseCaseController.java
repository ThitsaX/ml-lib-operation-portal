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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.TimeZoneOffsetFormater;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateSettlementBankReportUseCase;
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
public class GenerateSettlementBankReportUseCaseController {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateSettlementBankReportUseCaseController.class);

    private final GenerateSettlementBankReportUseCase generateSettlementBankReportUseCase;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/generateSettlementBankReportUseCase")
    public ResponseEntity<Response> execute(@RequestParam("settlementId") String settlementId,
                                            @RequestParam("currencyId") String currencyId,
                                            @RequestParam("fileType") String fileType,
                                            @RequestParam("timezoneOffset") String timezoneOffset)
            throws DomainException, JsonProcessingException {

        LOG.info(
                "Generate Settlement Bank Report Use Case : settlementId = [{}], currencyId = [{}], fileType = [{}], timezoneOffset = [{}]",
                settlementId,
                currencyId,
                fileType,
                timezoneOffset);

        String timezone = TimeZoneOffsetFormater.normalizeOffsetFormat(timezoneOffset);

        UserContext userContext =
                (UserContext) SecurityContextHolder.getContext()
                                                   .getAuthentication()
                                                   .getDetails();

        GenerateSettlementBankReportUseCase.Output output = this.generateSettlementBankReportUseCase.execute(
                new GenerateSettlementBankReportUseCase.Input(settlementId,
                                                              currencyId,
                                                              fileType,
                                                              timezone,
                                                              userContext.userId().getId()));

        var response = new Response(output.requestId().getEntityId().toString(),
                                    output.status().name(),
                                    output.fileUrl(),
                                    output.paramsSignature());

        LOG.info("Generate Settlement Bank Report Use Case Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public record Response(@JsonProperty("requestId") String requestId,
                           @JsonProperty("status") String status,
                           @JsonProperty("fileUrl") String fileUrl,
                           @JsonProperty("paramsSignature") String paramsSignature)
            implements Serializable { }
}
