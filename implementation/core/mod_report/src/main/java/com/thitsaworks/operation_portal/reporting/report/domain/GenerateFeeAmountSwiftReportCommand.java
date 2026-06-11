package com.thitsaworks.operation_portal.reporting.report.domain;

import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;

public interface GenerateFeeAmountSwiftReportCommand {

    record Input(String settlementId,
                 String currency,
                 String timezone
    ) {
    }

    record Output(byte[] feeSettlementRptByte) { }

    Output execute(Input input) throws ReportException;;
}
