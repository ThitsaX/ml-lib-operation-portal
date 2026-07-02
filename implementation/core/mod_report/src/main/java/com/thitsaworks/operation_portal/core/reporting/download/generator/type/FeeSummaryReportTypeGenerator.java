package com.thitsaworks.operation_portal.core.reporting.download.generator.type;

import com.thitsaworks.operation_portal.component.common.type.ReportType;
import com.thitsaworks.operation_portal.core.reporting.download.generator.ReportGeneratedFile;
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

    private final GenerateFeeSummaryReportCommand generateFeeSummaryReportCommand;

    private final ReportGeneratorSupport reportGeneratorSupport;

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

        GenerateFeeSummaryReportCommand.Output output = this.generateFeeSummaryReportCommand.execute(
            new GenerateFeeSummaryReportCommand.Input(settlementId, dfspId, timezoneOffset, fileType));

        return new ReportGeneratedFile(output.feeSummaryRptByte(), FILE_TYPE_XLSX);
    }

}
