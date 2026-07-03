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
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetLiquidityProfileList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetLiquidityProfileListController {

    private static final Logger LOG = LoggerFactory.getLogger(GetLiquidityProfileListController.class);

    private final GetLiquidityProfileList getLiquidityProfileList;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getLiquidityProfileList")
    public ResponseEntity<Response> execute(@RequestParam("participantId") String participantId)
        throws DomainException, JsonProcessingException {

        LOG.info("Get Liquidity Profile List Request : ParticipantId = [{}]", participantId);

        var
            output =
            this.getLiquidityProfileList.execute(new GetLiquidityProfileList.Input(new ParticipantId(Long.parseLong(
                participantId))));

        var
            response = new Response(output.liquidityProfileInfoList()
                                          .stream()
                                          .map(profile -> new Response.LiquidityProfileInfo(profile.liquidityProfileId()
                                                                                                   .getEntityId()
                                                                                                   .toString(),
                                                                                            profile.bankName(),
                                                                                            profile.accountName(),
                                                                                            profile.accountNumber(),
                                                                                            profile.currency(),
                                                                                            profile.isActive()))
                                          .toList());

        LOG.info("Get Liquidity Profile List Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<LiquidityProfileInfo> liquidityProfileInfoList) implements Serializable {

        public record LiquidityProfileInfo(@JsonProperty("liquidityProfileId") String liquidityProfileId,
                                           @JsonProperty("bankName") String bankName,
                                           @JsonProperty("accountName") String accountName,
                                           @JsonProperty("accountNumber") String accountNumber,
                                           @JsonProperty("currency") String currency,
                                           @JsonProperty("isActive") boolean isActive) implements Serializable {
        }

    }

}
