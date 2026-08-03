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
package com.thitsaworks.operation_portal.reporting.report.domain;

import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;

public interface GenerateRevenueSharingDetailReportCommand {

    record Input(String settlementId,
                 String fileType,
                 String timezoneOffset,
                 String taxCode,
                 String category,
                 Integer offset,
                 Integer limit) { }

    record Output(byte[] revenueSharingDetailRptByte) { }

    Output execute(Input input) throws ReportException;

    default Output exportAll(Input input, int totalRowCount, int pageSize)
        throws ReportException {

        throw new ReportException(ReportErrors.REVENUE_SHARING_DETAIL_REPORT_FAILURE_EXCEPTION);
    }

    record CountInput(String settlementId,
                      String taxCode,
                      String category) { }

    int countRows(CountInput input);

}
