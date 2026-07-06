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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeSettlementSummaryReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
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
class FeeSettlementSummaryReportTypeGenerator implements ReportTypeGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeeSettlementSummaryReportTypeGenerator.class);

    private static final String FILE_TYPE_XLSX = "xlsx";

    private static final int MAX_ROWS_PER_REPORT_FILE = 500_000;

    private final GenerateFeeSettlementSummaryReportCommand generateFeeSettlementSummaryReportCommand;

    private final ReportGeneratorSupport reportGeneratorSupport;

    private final ReportGenerator.Settings settings;

    @Override
    public ReportType reportType() {

        return ReportType.FEE_SETTLEMENT_SUMMARY;
    }

    @Override
    public ReportGeneratedFile generate(ReportDownloadRequest request, Map<String, String> params)
        throws ReportException, IOException {

        String fileType = this.reportGeneratorSupport.fileType(request.getFileType());
        if (!FILE_TYPE_XLSX.equals(fileType)) {
            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
        }

        String settlementId = this.reportGeneratorSupport.requireParam(params, "settlementId");
        String dfspId = this.reportGeneratorSupport.normalizeAllToken(
            this.reportGeneratorSupport.requireParam(params, "dfspId"));
        String timezoneOffset = params.getOrDefault("timezoneOffset", "+0000");
        String loginDfspId = params.getOrDefault("loginDfspId", dfspId);

        int pageSize = this.settings.reportPageSize();
        int totalRowCount = this.generateFeeSettlementSummaryReportCommand.countRows(
            new GenerateFeeSettlementSummaryReportCommand.CountInput(settlementId, dfspId));
        GenerateFeeSettlementSummaryReportCommand.Input input =
            new GenerateFeeSettlementSummaryReportCommand.Input(
                settlementId, dfspId, timezoneOffset, fileType, loginDfspId);

        if (totalRowCount <= pageSize) {
            GenerateFeeSettlementSummaryReportCommand.Output output =
                this.generateFeeSettlementSummaryReportCommand.execute(input);

            return new ReportGeneratedFile(output.feeSettlementSummaryRptByte(), FILE_TYPE_XLSX);
        }

        if (totalRowCount > MAX_ROWS_PER_REPORT_FILE) {
            return this.generateSplitZip(settlementId,
                                         dfspId,
                                         timezoneOffset,
                                         fileType,
                                         loginDfspId,
                                         totalRowCount,
                                         pageSize);
        }

        GenerateFeeSettlementSummaryReportCommand.Output output =
            this.generateFeeSettlementSummaryReportCommand.exportAll(input, totalRowCount, pageSize);

        return new ReportGeneratedFile(output.feeSettlementSummaryRptByte(), FILE_TYPE_XLSX);
    }

    private ReportGeneratedFile generateSplitZip(String settlementId,
                                                 String dfspId,
                                                 String timezoneOffset,
                                                 String fileType,
                                                 String loginDfspId,
                                                 int totalRowCount,
                                                 int pageSize) throws ReportException, IOException {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            int partNumber = 1;
            for (int offset = 0; offset < totalRowCount; offset += MAX_ROWS_PER_REPORT_FILE) {
                int rowsInPart = Math.min(MAX_ROWS_PER_REPORT_FILE, totalRowCount - offset);

                GenerateFeeSettlementSummaryReportCommand.Output partOutput =
                    this.generateFeeSettlementSummaryReportCommand.exportAll(
                        new GenerateFeeSettlementSummaryReportCommand.Input(
                            settlementId,
                            dfspId,
                            timezoneOffset,
                            fileType,
                            loginDfspId,
                            offset,
                            rowsInPart),
                        rowsInPart,
                        pageSize);

                String entryName = "fee_settlement_summary_part_" + partNumber + "." + fileType;
                ZipEntry entry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(partOutput.feeSettlementSummaryRptByte());
                zipOutputStream.closeEntry();
                LOGGER.info(
                    "Generated fee settlement summary report part [{}] with [{}] rows",
                    partNumber,
                    rowsInPart);
                partNumber++;
            }

            zipOutputStream.finish();
            return new ReportGeneratedFile(byteArrayOutputStream.toByteArray(), "zip");
        }
    }

}
