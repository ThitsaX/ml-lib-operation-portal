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
package com.thitsaworks.operation_portal.usecase.util;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;

import java.util.Locale;

public final class ReportDownloadUtil {

    private ReportDownloadUtil() {

    }

    public static String normalizeAllToken(String value) {

        if (value == null) {
            return "All";
        }

        return "all".equalsIgnoreCase(value.trim()) ? "All" : value.trim();
    }

    public static String normalizeFileType(String fileType) {

        return fileType == null ? "" : fileType.trim().toLowerCase(Locale.ROOT);
    }

    public static ErrorMessage resolveFailedError(String storedError, ErrorMessage defaultFailureError) {

        if (storedError == null || storedError.isBlank()) {
            return defaultFailureError;
        }

        int delimiterIndex = storedError.indexOf("-");
        String errorCode = delimiterIndex > 0 ? storedError.substring(0, delimiterIndex) : storedError;
        String errorDefaultMessage = delimiterIndex > 0 &&
            storedError.length() > delimiterIndex + 1 ? storedError.substring(delimiterIndex + 1) : "";

        return switch (errorCode) {
            case "RESULT_NOT_FOUND_EXCEPTION" -> withDefaultMessage(
                ReportErrors.RESULT_NOT_FOUND_EXCEPTION, errorDefaultMessage);
            case "FILE_FORMAT_NOT_ALLOWED_EXCEPTION" -> withDefaultMessage(
                ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION, errorDefaultMessage);
            case "REPORT_MAXIMUM_LIMIT_EXCEPTION" -> withDefaultMessage(
                ReportErrors.REPORT_MAXIMUM_LIMIT_EXCEPTION, errorDefaultMessage);
            case "AUDIT_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.AUDIT_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "TRANSACTION_DETAIL_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.TRANSACTION_DETAIL_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "SETTLEMENT_DETAIL_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.SETTLEMENT_DETAIL_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "SETTLEMENT_BANK_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.SETTLEMENT_BANK_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "SETTLEMENT_BANK_USECASE_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.SETTLEMENT_BANK_USECASE_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "SETTLEMENT_STATEMENT_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.STATEMENT_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "SETTLEMENT_AUDIT_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.SETTLEMENT_AUDIT_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "FEE_SETTLEMENT_SUMMARY_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.FEE_SETTLEMENT_SUMMARY_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            case "FEE_SUMMARY_REPORT_FAILURE_EXCEPTION" -> withDefaultMessage(
                ReportErrors.FEE_SUMMARY_REPORT_FAILURE_EXCEPTION, errorDefaultMessage);
            default -> defaultFailureError.defaultMessage(storedError);
        };
    }

    private static ErrorMessage withDefaultMessage(ErrorMessage errorMessage, String defaultMessage) {

        if (defaultMessage == null || defaultMessage.isBlank()) {
            return errorMessage;
        }

        return errorMessage.defaultMessage(defaultMessage);
    }
}
