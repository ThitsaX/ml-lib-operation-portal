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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetHubCurrencyList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetHubCurrenciesController {

    private static final Logger LOG = LoggerFactory.getLogger(GetHubCurrenciesController.class);

    private final GetHubCurrencyList getHubCurrencyList;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getHubCurrency")
    public ResponseEntity<Response> execute() throws DomainException, JsonProcessingException {

        var output = this.getHubCurrencyList.execute(new GetHubCurrencyList.Input());

        List<CurrencyInfo>
            currencyInfoList =
            output.currencyDataList()
                  .stream()
                  .map(idType -> new CurrencyInfo(idType.getCurrency()))
                  .toList();

        var response = new Response(currencyInfoList);

        LOG.info("Get Hub Currencies Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("currencyInfoList") List<CurrencyInfo> currencyInfoList)
        implements Serializable { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrencyInfo(@JsonProperty("currency") String currency) implements Serializable { }

}
