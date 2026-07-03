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
package com.thitsaworks.operation_portal.component.security;

import com.google.common.io.BaseEncoding;
import com.thitsaworks.operation_portal.component.misc.security.OperationPortalCrypto;
import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class OperationPortalCryptoUnitTest extends EnvAwareUnitTest {

    @Test
    public void test_hmacsha256() {

        String method = "POST";

        String uri = "/secured/create_new_user";
        String signatureOfPayload = DigestUtils.sha256Hex(
                                                       "{\"name\":\"wallet2 admin\",\"email\":\"wallet2admin@gmail.com\",\"password\":\"123456\",\"first_name\":\"Wallet 2\",\"last_name\":\"admin\",\"job_title\":\"PM\",\"participant_id\":\"486552745708363776\",\"user_role_type\":\"ADMIN\",\"is_active\":true}").toUpperCase();

        String message = method + "|" + uri + "|" + signatureOfPayload;

        System.out.println("signatureOfPayload : " + signatureOfPayload);
        System.out.println("message : " + message);
        System.out.println("header : " + BaseEncoding.base16().encode(OperationPortalCrypto.hmacSha256(
                "ea3184c0-0c70-4ab5-af24-adb3ac3b6885".getBytes(StandardCharsets.UTF_8),
                message.getBytes(StandardCharsets.UTF_8))));

    }

}
