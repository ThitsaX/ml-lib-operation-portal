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
package com.thitsaworks.operation_portal.api.operation.portal.controller.engineServices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.core.iam.data.EngineData;
import com.thitsaworks.operation_portal.usecase.operation_portal.DumpIAMEngineState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class DumpIAMEngineStateController {

    private static final Logger LOG = LoggerFactory.getLogger(DumpIAMEngineStateController.class);

    private final DumpIAMEngineState dumpIAMEngineState;

    @GetMapping("/secured/dumpIAMEngineState")
    public ResponseEntity<Response> execute() throws
                                              DomainException {

        var output = this.dumpIAMEngineState.execute(new DumpIAMEngineState.Input());

        var response = new Response(output.engineState());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public record Response(@JsonProperty("iamEngineState") EngineData engineState) implements Serializable { }

}
