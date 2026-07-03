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
import com.thitsaworks.operation_portal.usecase.operation_portal.GetIDTypeList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllIDTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(GetAllIDTypeController.class);

    private final GetIDTypeList getIDTypeList;

    private final ObjectMapper objectMapper;

    @GetMapping("/secured/getAllIdType")
    public ResponseEntity<Response> execute() throws DomainException, JsonProcessingException {

        GetIDTypeList.Output output = this.getIDTypeList.execute(new GetIDTypeList.Input());

        List<Response.IDTypeInfo> idTypeInfoList = new ArrayList<>();

        for (var idType : output.idTypeDataList()) {

            idTypeInfoList.add(new Response.IDTypeInfo(
                idType.getPartyIdentifierTypeId(),
                idType.getName()));
        }

        var response = new Response(idTypeInfoList);

        LOG.info("Get All ID Type Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        @JsonProperty("idTypeInfoList") List<IDTypeInfo> idTypeInfoList
    ) implements Serializable {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record IDTypeInfo(
            @JsonProperty("partyIdentifierTypeId") String partyIdentifierTypeId,
            @JsonProperty("name") String name
        ) implements Serializable { }

    }

}
