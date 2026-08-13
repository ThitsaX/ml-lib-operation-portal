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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateRevenueSharingSummaryReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
class RevenueSharingSummaryReportTypeGenerator implements ReportTypeGenerator {

    private static final int MAX_ROWS_PER_REPORT_FILE = 500_000;

    private final GenerateRevenueSharingSummaryReportCommand generateRevenueSharingSummaryReportCommand;

    private final ReportGeneratorSupport reportGeneratorSupport;

    private final ReportGenerator.Settings settings;

    @Override
    public ReportType reportType() {

        return ReportType.REVENUE_SHARING_SUMMARY;
    }

    @Override
    public ReportGeneratedFile generate(ReportDownloadRequest request, Map<String, String> params)
        throws ReportException, IOException {

        String fileType = this.reportGeneratorSupport.fileType(request.getFileType());

        String settlementId = params.getOrDefault("settlementId", "");
        String timezoneOffset = params.getOrDefault("timezoneOffset", "+0000");
        int totalRowCount = this.generateRevenueSharingSummaryReportCommand.countRows(
            new GenerateRevenueSharingSummaryReportCommand.CountInput(settlementId, timezoneOffset));
        if (totalRowCount <= 0) {
            throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
        }

        int pageSize = this.settings.reportPageSize();
        String date = params.getOrDefault("date", "");
        GenerateRevenueSharingSummaryReportCommand.Input input =
            new GenerateRevenueSharingSummaryReportCommand.Input(
                date,
                settlementId,
                timezoneOffset,
                fileType,
                0,
                pageSize);

        if (totalRowCount <= pageSize) {
            GenerateRevenueSharingSummaryReportCommand.Output output =
                this.generateRevenueSharingSummaryReportCommand.execute(input);

            return new ReportGeneratedFile(output.revenueSharingSummaryRptByte(), fileType);
        }

        if (totalRowCount > MAX_ROWS_PER_REPORT_FILE) {
            return this.generateSplitZip(date, settlementId, timezoneOffset, fileType, totalRowCount, pageSize);
        }

        GenerateRevenueSharingSummaryReportCommand.Output output =
            this.generateRevenueSharingSummaryReportCommand.exportAll(input, totalRowCount, pageSize);

        return new ReportGeneratedFile(output.revenueSharingSummaryRptByte(), fileType);
    }

    private ReportGeneratedFile generateSplitZip(String date,
                                                 String settlementId,
                                                 String timezoneOffset,
                                                 String fileType,
                                                 int totalRowCount,
                                                 int pageSize) throws ReportException, IOException {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            int partNumber = 1;
            for (int offset = 0; offset < totalRowCount; offset += MAX_ROWS_PER_REPORT_FILE) {
                int rowsInPart = Math.min(MAX_ROWS_PER_REPORT_FILE, totalRowCount - offset);

                GenerateRevenueSharingSummaryReportCommand.Output partOutput =
                    this.generateRevenueSharingSummaryReportCommand.exportAll(
                        new GenerateRevenueSharingSummaryReportCommand.Input(
                            date,
                            settlementId,
                            timezoneOffset,
                            fileType,
                            offset,
                            rowsInPart),
                        rowsInPart,
                        pageSize);

                String entryName = "revenue_sharing_summary_part_" + partNumber + "." + fileType;
                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                zipOutputStream.write(partOutput.revenueSharingSummaryRptByte());
                zipOutputStream.closeEntry();
                partNumber++;
            }

            zipOutputStream.finish();
            return new ReportGeneratedFile(byteArrayOutputStream.toByteArray(), "zip");
        }
    }
}
