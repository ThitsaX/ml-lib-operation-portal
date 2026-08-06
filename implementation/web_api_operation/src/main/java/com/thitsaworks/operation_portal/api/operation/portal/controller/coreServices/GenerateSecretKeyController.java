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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.component.misc.util.MaskPassword;
import com.thitsaworks.operation_portal.usecase.operation_portal.LoginUserAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class GenerateSecretKeyController {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateSecretKeyController.class);

    private final LoginUserAccount loginUserAccount;

    private final ObjectMapper objectMapper;

    @PostMapping("/public/generateSecretKey")
    public ResponseEntity<Response> execute(@Valid @RequestBody Request request)
        throws Exception {

        LOG.info("Generate Secret Key Request : [{}]",
                 MaskPassword.maskPassword(this.objectMapper,
                                           this.objectMapper.writeValueAsString(request)));

        LoginUserAccount.Output output = this.loginUserAccount.execute(
            new LoginUserAccount.Input(new Email(request.username()), request.password()));

        var response = new Response(output.accessKey()
                                          .getId()
                                          .toString(), output.secretKey());

        LOG.info("Generate Secret Key Response : [{}]",
                 MaskPassword.maskPassword(this.objectMapper,
                                           this.objectMapper.writeValueAsString(response)));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        @NotBlank
        @Pattern(
            regexp = Email.FORMAT,
            message = "Username must contain a valid administrator email address.")
        @JsonProperty("username")
        String username,

        @NotBlank
        @JsonProperty("password")
        String password) implements Serializable {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("accessKey") String accessKey,
                           @JsonProperty("secretKey") String secretKey) implements Serializable {

    }

}
