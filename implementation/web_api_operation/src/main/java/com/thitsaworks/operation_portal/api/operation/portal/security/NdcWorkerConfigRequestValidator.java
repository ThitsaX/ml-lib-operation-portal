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
package com.thitsaworks.operation_portal.api.operation.portal.security;

import com.thitsaworks.operation_portal.api.operation.portal.error.ErrorResponse;
import com.thitsaworks.operation_portal.api.operation.portal.security.exception.SecurityErrors;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import org.springframework.scheduling.support.CronExpression;


public final class NdcWorkerConfigRequestValidator {


    private NdcWorkerConfigRequestValidator() {
    }

    public static Values validate(String runEvery) {

        String cronExpression = toCronExpression(runEvery);

        return new Values( cronExpression);
    }


    private static String toCronExpression(String runEvery) {

        String[] parts = runEvery.trim().split(":");

        if (parts.length != 3) {
            throw invalid("runEvery must use HH:mm:ss interval format.");
        }

        int hour = parsePart(parts[0], "hour");
        int minute = parsePart(parts[1], "minute");
        int second = parsePart(parts[2], "second");

        if (minute > 59) {
            throw invalid("runEvery minute must be between 00 and 59.");
        }

        if (second > 59) {
            throw invalid("runEvery second must be between 00 and 59.");
        }

        if (hour == 0 && minute == 0 && second == 0) {
            throw invalid("runEvery must be greater than 00:00:00.");
        }

        String cronExpression;

        if (hour > 0 && minute == 0 && second == 0) {
            cronExpression = "0 0 */" + hour + " * * ?";
        } else if (hour == 0 && minute > 0 && second == 0) {
            cronExpression = "0 */" + minute + " * * * ?";
        } else if (hour == 0 && minute == 0) {
            cronExpression = "*/" + second + " * * * * ?";
        } else {
            throw invalid("runEvery supports one interval unit only: HH:00:00, 00:mm:00, or 00:00:ss.");
        }

        validateCronExpression(cronExpression);

        return cronExpression;
    }

    private static int parsePart(String value, String label) {

        if (!value.matches("\\d+")) {
            throw invalid("runEvery " + label + " must be numeric.");
        }

        return Integer.parseInt(value);
    }

    private static void validateCronExpression(String cronExpression) {

        try {
            CronExpression.parse(cronExpression);
        } catch (IllegalArgumentException e) {
            throw invalid("runEvery cannot be converted to a valid cron expression.");
        }
    }


    private static InputException invalid(String message) {

        return new InputException(SecurityErrors.INVALID_NDC_WORKER_CONFIG.defaultMessage(message));
    }

    public record Values(String cronExpression) {
    }
}
