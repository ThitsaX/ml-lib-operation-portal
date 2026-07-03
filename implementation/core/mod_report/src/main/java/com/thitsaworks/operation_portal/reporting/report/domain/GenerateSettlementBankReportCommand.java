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

import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;

public interface GenerateSettlementBankReportCommand {

    record Input(String settlementId,
                 String currencyId,
                 String fileType,
                 String timezoneOffset,
                 String userName,
                 String dfspId,
                 boolean isParent,
                 Integer offset,
                 Integer limit) { }

    record Output(byte[] settlementBankRptByte) { }

    Output execute(Input input) throws ReportException;

    record CountInput(String settlementId, String currencyId, String dfspId) { }

    int countRows(CountInput input);

}
