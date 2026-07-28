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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.RevenueTransactionId;
import com.thitsaworks.operation_portal.component.common.type.TransactionState;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenueTransaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ModifyRevenueTransactionController {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyRevenueTransactionController.class);

    private final ModifyRevenueTransaction modifyRevenueTransaction;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/secured/modifyRevenueTransaction")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request) throws Exception {

        LOG.info("Modify Revenue Transaction Request : [{}]",
                 this.objectMapper.writeValueAsString(request));

        var output = this.modifyRevenueTransaction.execute(new ModifyRevenueTransaction.Input(
                new RevenueTransactionId(request.revenueTransactionId()),
                TransactionState.valueOf(request.state().toUpperCase())));

        var response = new Response(output.modified(),
                                    output.revenueTransactionId().getId().toString(),
                                    output.transactionDetails());

        LOG.info("Modify Revenue Transaction Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(@NotNull @JsonProperty("revenueTransactionId") Long revenueTransactionId,
                          @NotBlank @JsonProperty("state") String state) implements Serializable {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("modified") boolean modified,
                           @JsonProperty("revenueTransactionId") String revenueTransactionId,
                           @JsonProperty("transactionDetails")
                           List<ModifyRevenueTransaction.TransactionDetail> transactionDetails)
            implements Serializable {
    }
}
