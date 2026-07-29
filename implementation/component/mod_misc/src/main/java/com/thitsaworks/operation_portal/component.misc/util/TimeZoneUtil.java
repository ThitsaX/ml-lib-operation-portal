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
package com.thitsaworks.operation_portal.component.misc.util;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeZoneUtil {

    private static final Pattern GMT_OFFSET_PATTERN = Pattern.compile(
        "\\b(?:GMT|UTC)\\s*([+-]\\d{2}:?\\d{2})?\\b", Pattern.CASE_INSENSITIVE);

    public static ZoneId zoneId(String timezone) {

        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }

        try {
            return ZoneId.of(timezone.trim());
        } catch (DateTimeException e) {
            Matcher matcher = GMT_OFFSET_PATTERN.matcher(timezone);
            if (!matcher.find()) {
                return ZoneOffset.UTC;
            }

            String offset = matcher.group(1);
            if (offset == null || offset.isBlank()) {
                return ZoneOffset.UTC;
            }

            return ZoneId.of("GMT" + normalizeOffset(offset));
        }
    }

    private static String normalizeOffset(String offset) {

        String normalizedOffset = offset.replace(" ", "");
        return normalizedOffset.contains(":") ? normalizedOffset :
                   normalizedOffset.substring(0, 3) + ":" + normalizedOffset.substring(3);
    }
}
