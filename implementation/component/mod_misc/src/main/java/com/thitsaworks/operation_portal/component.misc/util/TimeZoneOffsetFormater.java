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

public class TimeZoneOffsetFormater {

    public static String normalizeOffset(String rawOffset) {

        if (rawOffset == null || rawOffset.isBlank()) {
            throw new IllegalArgumentException("Offset cannot be null or blank");
        }

        rawOffset = rawOffset.trim().toUpperCase();

        if (rawOffset.equals("Z") || rawOffset.equals("UTC") || rawOffset.equals("GMT")) {
            return "+00:00";
        }

        if (rawOffset.startsWith("UTC") || rawOffset.startsWith("GMT")) {
            rawOffset = rawOffset.substring(3).trim();
        }

        char sign = rawOffset.charAt(0);

        if (sign != '-') {
            if (!Character.isDigit(sign)) {
                rawOffset = rawOffset.substring(1);
            }

            sign = '+';

        } else {
            rawOffset = rawOffset.substring(1);
        }

        rawOffset = rawOffset.replace(":", "");

        if (rawOffset.length() < 4) {
            rawOffset = String.format("%04d", Integer.parseInt(rawOffset));
        }

        String hours = rawOffset.substring(0, 2);
        String minutes = rawOffset.substring(2, 4);

        return sign + hours + ":" + minutes;
    }

    //    Keeps negative sign.
    //    Drops positive “+”.
    //    Removes : if present.
    //    Pads to 4 digits for consistency.
    //    Converts Z/UTC/GMT → 0000.
    public static String normalizeOffsetFormat(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Offset cannot be null or blank");
        }

        String s = input.trim()
                        .toUpperCase()
                        .trim();

        if (s.equals("Z") || s.equals("UTC") || s.equals("GMT")) {
            return "0000";
        }

        boolean negative = s.startsWith("-");
        if (negative || s.startsWith("+")) {
            s = s.substring(1);
        }

        s = s.replace(":", "");

        if (!s.matches("\\d{1,4}")) {
            throw new IllegalArgumentException("Invalid offset format: " + input);
        }

        if (s.length() == 1) {
            s = "0" + s + "00";
        } else if (s.length() == 2) {
            s = s + "00";
        } else if (s.length() == 3) {
            s = "0" + s;
        }

        return negative ? "-" + s : s;
    }

}
