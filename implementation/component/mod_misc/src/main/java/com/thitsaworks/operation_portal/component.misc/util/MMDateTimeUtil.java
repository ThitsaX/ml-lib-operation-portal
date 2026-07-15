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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MMDateTimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss O")
                                                                        .withZone(ZoneId.of("GMT+06:30"));

    public static String toString(Instant instant) {

        return FORMATTER.format(instant);

    }

    public static MMLocalTime nowInMyanmar() {

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(("GMT+06:30")));

        return new MMLocalTime(zonedDateTime.getYear(), zonedDateTime.getMonth().getValue(),
                zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute(),
                zonedDateTime.getSecond());
    }

    public static  MMLocalTime toMyanmarTime(Instant instant){

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of(("GMT+06:30")));

        return new MMLocalTime(zonedDateTime.getYear(), zonedDateTime.getMonth().getValue(),
                zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute(),
                zonedDateTime.getSecond());
    }

    public static class MMLocalTime {

        private final int year;

        private final int month;

        private final int date;

        private final int hour;

        private final int minute;

        private final int second;

        public MMLocalTime(int year, int month, int date, int hour, int minute, int second) {

            this.year = year;
            this.month = month;
            this.date = date;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public int getYear() {

            return this.year;
        }

        public int getMonth() {

            return this.month;
        }

        public int getDate() {

            return this.date;
        }

        public int getHour() {

            return this.hour;
        }

        public int getMinute() {

            return this.minute;
        }

        public int getSecond() {

            return this.second;
        }

    }

}
