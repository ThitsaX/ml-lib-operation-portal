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
package com.thitsaworks.operation_portal.core.reporting.download.request;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Map;

final class ReportRequestSignature {

    private ReportRequestSignature() {

    }

    static String from(String reportType, String fileType, Map<String, String> params) {

        StringBuilder canonical = new StringBuilder();
        canonical.append(normalize(reportType))
                 .append("|")
                 .append(normalize(fileType));

        params.entrySet()
              .stream()
              .sorted(Comparator.comparing(Map.Entry::getKey))
              .forEach(entry -> canonical.append("|")
                                         .append(entry.getKey())
                                         .append("=")
                                         .append(normalize(entry.getValue())));

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte value : hash) {

                hex.append(String.format("%02x", value));
            }

            return hex.toString();

        } catch (Exception e) {

            throw new IllegalStateException("Unable to generate request signature", e);
        }
    }

    private static String normalize(String value) {

        return value == null ? "" : value.trim();
    }
}
