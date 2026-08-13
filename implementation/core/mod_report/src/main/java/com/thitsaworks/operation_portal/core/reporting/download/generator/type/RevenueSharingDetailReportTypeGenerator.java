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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateRevenueSharingDetailReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
class RevenueSharingDetailReportTypeGenerator implements ReportTypeGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevenueSharingDetailReportTypeGenerator.class);

    private static final int MAX_ROWS_PER_REPORT_FILE = 500_000;

    private final GenerateRevenueSharingDetailReportCommand generateRevenueSharingDetailReportCommand;

    private final ReportGeneratorSupport reportGeneratorSupport;

    private final ReportGenerator.Settings settings;

    @Override
    public ReportType reportType() {

        return ReportType.REVENUE_SHARING_DETAIL;
    }

    @Override
    public ReportGeneratedFile generate(ReportDownloadRequest request, Map<String, String> params)
        throws ReportException, IOException {

        String fileType = this.reportGeneratorSupport.fileType(request.getFileType());

        String settlementId = this.reportGeneratorSupport.requireParam(params, "settlementId");
        String timezoneOffset = params.getOrDefault("timezoneOffset", "+0000");
        String taxCode = this.reportGeneratorSupport.normalizeAllToken(params.getOrDefault("taxCode", "ALL"));
        String category = this.reportGeneratorSupport.normalizeAllToken(params.getOrDefault("category", "ALL"));

        int pageSize = this.settings.reportPageSize();
        int totalRowCount = this.generateRevenueSharingDetailReportCommand.countRows(
            new GenerateRevenueSharingDetailReportCommand.CountInput(settlementId, taxCode, category));
        GenerateRevenueSharingDetailReportCommand.Input input =
            new GenerateRevenueSharingDetailReportCommand.Input(
                settlementId, fileType, timezoneOffset, taxCode, category, 0, pageSize);

        if (totalRowCount <= pageSize) {
            GenerateRevenueSharingDetailReportCommand.Output output =
                this.generateRevenueSharingDetailReportCommand.execute(input);

            return new ReportGeneratedFile(output.revenueSharingDetailRptByte(), fileType);
        }

        if (totalRowCount > MAX_ROWS_PER_REPORT_FILE) {
            return this.generateSplitZip(
                settlementId, fileType, timezoneOffset, taxCode, category, totalRowCount, pageSize);
        }

        GenerateRevenueSharingDetailReportCommand.Output output =
            this.generateRevenueSharingDetailReportCommand.exportAll(input, totalRowCount, pageSize);

        return new ReportGeneratedFile(output.revenueSharingDetailRptByte(), fileType);
    }

    private ReportGeneratedFile generateSplitZip(String settlementId,
                                                 String fileType,
                                                 String timezoneOffset,
                                                 String taxCode,
                                                 String category,
                                                 int totalRowCount,
                                                 int pageSize) throws ReportException, IOException {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            int partNumber = 1;
            for (int offset = 0; offset < totalRowCount; offset += MAX_ROWS_PER_REPORT_FILE) {
                int rowsInPart = Math.min(MAX_ROWS_PER_REPORT_FILE, totalRowCount - offset);

                GenerateRevenueSharingDetailReportCommand.Output partOutput =
                    this.generateRevenueSharingDetailReportCommand.exportAll(
                        new GenerateRevenueSharingDetailReportCommand.Input(
                            settlementId,
                            fileType,
                            timezoneOffset,
                            taxCode,
                            category,
                            offset,
                            rowsInPart),
                        rowsInPart,
                        pageSize);

                String entryName = "revenue_sharing_detail_part_" + partNumber + "." + fileType;
                ZipEntry entry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(partOutput.revenueSharingDetailRptByte());
                zipOutputStream.closeEntry();
                LOGGER.info(
                    "Generated revenue sharing detail report part [{}] with [{}] rows",
                    partNumber,
                    rowsInPart);
                partNumber++;
            }

            zipOutputStream.finish();
            return new ReportGeneratedFile(byteArrayOutputStream.toByteArray(), "zip");
        }
    }

}
