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
import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenuePartyList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetRevenuePartyListController {

    private static final Logger LOG = LoggerFactory.getLogger(GetRevenuePartyListController.class);

    private final GetRevenuePartyList getRevenuePartyList;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getRevenuePartyList")
    public ResponseEntity<Response> execute(
        @RequestParam(value = "partyType", required = false) String partyType,
        @RequestParam(value = "isActive", required = false) Boolean isActive,
        @RequestParam(value = "searchText", required = false) String searchText,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "sortDirection", required = false) Sort.Direction sortDirection)
        throws DomainException, JsonProcessingException {

        LOG.info("Get Revenue Party List Request : partyType= [{}], isActive= [{}], searchText= [{}]",
                 partyType, isActive, searchText);

        var output = this.getRevenuePartyList.execute(
            new GetRevenuePartyList.Input(partyType, isActive, searchText, page, size, sortBy, sortDirection));
        var response = new Response(output.revenueParties(), output.totalElements(), output.totalPages(),
                                    output.page(), output.size());

        LOG.info("Get Revenue Party List Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("revenueParties") List<RevenuePartyData> revenueParties,
                           @JsonProperty("totalElements") long totalElements,
                           @JsonProperty("totalPages") int totalPages,
                           @JsonProperty("page") int page,
                           @JsonProperty("size") int size) { }

}
