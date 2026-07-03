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
import com.thitsaworks.operation_portal.component.common.type.ActionCode;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GrantMenuActionList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GrantMenuActionListController {

    private static final Logger LOG = LoggerFactory.getLogger(GrantMenuActionListController.class);

    private final GrantMenuActionList grantMenuActionList;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/grantMenuActionList")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws DomainException, JsonProcessingException {

        LOG.info("Grant Menu Action List Request : [{}]", this.objectMapper.writeValueAsString(request));

        List<GrantMenuActionList.Input.MenuGrant> menuGrantList = new ArrayList<>();
        for (var singleMenuGrant : request.menuGrantList()) {

            List<ActionCode> actionCodeList = new ArrayList<>();

            for (var action : singleMenuGrant.actionCodeList()) {
                actionCodeList.add(new ActionCode(action));
            }

            menuGrantList.add(new GrantMenuActionList.Input.MenuGrant(singleMenuGrant.menuName(),
                                                                      actionCodeList));

        }

        var output = this.grantMenuActionList.execute(new GrantMenuActionList.Input(menuGrantList));

        var response = new Response(output.granted());

        LOG.info("Grant Menu Action List Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(List<MenuGrant> menuGrantList) implements Serializable {

        public record MenuGrant(@JsonProperty("menuName") String menuName,
                                @JsonProperty("actionCodeList") List<String> actionCodeList) implements Serializable { }

    }

    public record Response(@JsonProperty("granted") boolean granted) implements Serializable { }

}
