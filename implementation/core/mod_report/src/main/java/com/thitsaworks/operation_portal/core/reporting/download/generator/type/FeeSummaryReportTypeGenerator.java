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
import com.thitsaworks.operation_portal.core.reporting.download.generator.ReportGenerator;
import com.thitsaworks.operation_portal.core.reporting.download.generator.ReportTypeGenerator;
import com.thitsaworks.operation_portal.core.reporting.download.generator.support.ReportGeneratorSupport;
import com.thitsaworks.operation_portal.core.reporting.download.model.ReportDownloadRequest;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeSummaryReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
class FeeSummaryReportTypeGenerator implements ReportTypeGenerator {

    private static final String FILE_TYPE_XLSX = "xlsx";

    private static final String FILE_TYPE_PDF = "pdf";

    private final GenerateFeeSummaryReportCommand generateFeeSummaryReportCommand;

    private final ReportGeneratorSupport reportGeneratorSupport;

    private final ReportGenerator.Settings settings;

    @Override
    public ReportType reportType() {

        return ReportType.FEE_SUMMARY;
    }

    @Override
    public ReportGeneratedFile generate(ReportDownloadRequest request, Map<String, String> params)
        throws ReportException, IOException {

        String fileType = this.reportGeneratorSupport.fileType(request.getFileType());
        if (!FILE_TYPE_XLSX.equals(fileType) && !FILE_TYPE_PDF.equals(fileType)) {
            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
        }

        String startDate = this.reportGeneratorSupport.requireParam(params, "startDate");
        String endDate = this.reportGeneratorSupport.requireParam(params, "endDate");
        String dfspId = this.reportGeneratorSupport.normalizeAllToken(
            this.reportGeneratorSupport.requireParam(params, "dfspId"));
        String timezoneOffset = params.getOrDefault("timezoneOffset", "+0000");
        String loginDfspId = params.getOrDefault("loginDfspId", dfspId);

        int pageSize = this.settings.reportPageSize();
        int totalRowCount = this.generateFeeSummaryReportCommand.countRows(
            new GenerateFeeSummaryReportCommand.CountInput(startDate, endDate, dfspId, timezoneOffset));
        GenerateFeeSummaryReportCommand.Input input =
            new GenerateFeeSummaryReportCommand.Input(
                startDate, endDate, dfspId, timezoneOffset, fileType, loginDfspId);

        if (totalRowCount > pageSize) {
            GenerateFeeSummaryReportCommand.Output output =
                this.generateFeeSummaryReportCommand.exportAll(input, totalRowCount, pageSize);

            return new ReportGeneratedFile(output.feeSummaryRptByte(), fileType);
        }

        GenerateFeeSummaryReportCommand.Output output =
            this.generateFeeSummaryReportCommand.execute(input);

        return new ReportGeneratedFile(output.feeSummaryRptByte(), fileType);
    }

}
