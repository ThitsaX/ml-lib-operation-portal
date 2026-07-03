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
import com.thitsaworks.operation_portal.component.common.identifier.SettlementModelId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetSettlementSchedulerList;
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
public class GetSettlementSchedulerListController {

    private static final Logger LOG = LoggerFactory.getLogger(GetSettlementSchedulerListController.class);

    private final GetSettlementSchedulerList getSettlementSchedulerList;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getSettlementSchedulerList")
    public ResponseEntity<Response> execute(@RequestParam("settlementModelId") String settlementModelId)
        throws DomainException, JsonProcessingException {

        var output = this.getSettlementSchedulerList.execute(new GetSettlementSchedulerList.Input(new SettlementModelId(
            Long.parseLong(settlementModelId))));

        List<Response.SettlementSchedulerData>
            settlementSchedulerList = output.schedulerConfigDataList()
                                            .stream()
                                            .map(sc -> new Response.SettlementSchedulerData(
                                                sc.schedulerConfigId()
                                                  .getEntityId()
                                                  .toString(),
                                                sc.name(),
                                                sc.jobName(),
                                                sc.cronExpression(),
                                                sc.description(),
                                                sc.zoneId(),
                                                sc.active()))
                                            .toList();

        Response response = new Response(settlementSchedulerList);

        LOG.info("Get Settlement Scheduler List Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<SettlementSchedulerData> settlementSchedulerList) implements Serializable {

        public record SettlementSchedulerData(
            @JsonProperty("schedulerConfigId") String schedulerConfigId,
            @JsonProperty("name") String name,
            @JsonProperty("jobName") String jobName,
            @JsonProperty("cronExpression") String cronExpression,
            @JsonProperty("description") String description,
            @JsonProperty("zoneId") String zoneId,
            @JsonProperty("active") boolean active) implements Serializable { }

    }

}
