package com.thitsaworks.operation_portal.reporting.report.domain;

import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;

public interface GenerateFeeSettlementSummaryReportCommand {

    record Input(String settlementId,
                 String dfspId,
                 String timezone,
                 String fileType,
                 String loginDfspId,
                 Integer offset,
                 Integer limit) {

        public Input(String settlementId,
                     String dfspId,
                     String timezone,
                     String fileType,
                     String loginDfspId) {

            this(settlementId, dfspId, timezone, fileType, loginDfspId, null, null);
        }
    }

    record Output(byte[] feeSettlementSummaryRptByte) { }

    Output execute(Input input) throws ReportException;

    default Output exportAll(Input input, int totalRowCount, int pageSize)
        throws ReportException {

        throw new ReportException(ReportErrors.FEE_SETTLEMENT_SUMMARY_REPORT_FAILURE_EXCEPTION);
    }

    record CountInput(String settlementId,
                      String dfspId) { }

    int countRows(CountInput input);
}
