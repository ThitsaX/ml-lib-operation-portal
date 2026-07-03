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
package com.thitsaworks.operation_portal.component.misc.security;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class OperationPortalCrypto {

    private static final Logger LOG = LoggerFactory.getLogger(OperationPortalCrypto.class);

    public static byte[] hmacSha256(byte[] secretKey, byte[] message) {

        byte[] hmacSha256 = null;

        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");

            mac.init(secretKeySpec);

            hmacSha256 = mac.doFinal(message);

        } catch (Exception e) {

            throw new RuntimeException("Failed to calculate hmac-sha256", e);

        }

        return hmacSha256;

    }

    public static String sha256Hex(String input) {

        return BaseEncoding.base16().encode(Hashing.sha256().hashString(input, Charsets.UTF_8).asBytes());

    }

    public static String secretKeyHex() {

        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[20];

        random.nextBytes(bytes);

        return BaseEncoding.base16().encode(bytes);

    }

}
