package com.thitsaworks.operation_portal.reporting.report.domain;

import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;

public interface GenerateFeeSummaryReportCommand {

    record Input(String settlementId,
                 String dfspId,
                 String timezone,
                 String fileType) {
    }

    record Output(byte[] feeSummaryRptByte) { }

    Output execute(Input input) throws ReportException;
}
