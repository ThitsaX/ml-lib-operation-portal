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
package com.thitsaworks.operation_portal.core.reporting.download.generator.type;

import com.thitsaworks.operation_portal.component.common.type.ReportType;
import com.thitsaworks.operation_portal.core.reporting.download.generator.ReportGeneratedFile;
import com.thitsaworks.operation_portal.core.reporting.download.generator.ReportTypeGenerator;
import com.thitsaworks.operation_portal.core.reporting.download.generator.support.ReportGeneratorSupport;
import com.thitsaworks.operation_portal.core.reporting.download.model.ReportDownloadRequest;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateRevenueSharingSummaryReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
class RevenueSharingSummaryReportTypeGenerator implements ReportTypeGenerator {

    private static final String FILE_TYPE_XLSX = "xlsx";

    private final GenerateRevenueSharingSummaryReportCommand generateRevenueSharingSummaryReportCommand;

    private final ReportGeneratorSupport reportGeneratorSupport;

    @Override
    public ReportType reportType() {

        return ReportType.REVENUE_SHARING_SUMMARY;
    }

    @Override
    public ReportGeneratedFile generate(ReportDownloadRequest request, Map<String, String> params)
        throws ReportException, IOException {

        String fileType = this.reportGeneratorSupport.fileType(request.getFileType());
        if (!FILE_TYPE_XLSX.equals(fileType)) {
            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
        }

        GenerateRevenueSharingSummaryReportCommand.Output output =
            this.generateRevenueSharingSummaryReportCommand.execute(
                new GenerateRevenueSharingSummaryReportCommand.Input(
                    params.getOrDefault("date", ""),
                    params.getOrDefault("settlementId", ""),
                    params.getOrDefault("timezoneOffset", "+0000"),
                    fileType,
                    0,
                    Integer.MAX_VALUE));

        return new ReportGeneratedFile(output.revenueSharingSummaryRptByte(), FILE_TYPE_XLSX);
    }
}
