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
package com.thitsaworks.operation_portal.core.hub_services.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.fspiop.model.ErrorInformation;
import com.thitsaworks.operation_portal.component.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.operation_portal.component.misc.retrofit.RetrofitRunner;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;

import java.io.IOException;

@RequiredArgsConstructor
public class HubApiErrorDecoder implements RetrofitRunner.ErrorDecoder<ErrorInformationResponse> {

    private final ObjectMapper objectMapper;

    @Override
    public ErrorInformationResponse decode(int status, ResponseBody errorResponseBody) {

        try {

            return this.objectMapper.readValue(errorResponseBody.string(), ErrorInformationResponse.class);

        } catch (IOException e) {

            ErrorInformation errorInformation = new ErrorInformation();

            errorInformation.setErrorCode("-1111");

            errorInformation.errorDescription("Something went wrong.");

            return new ErrorInformationResponse().errorInformation(errorInformation);

        }

    }

}
