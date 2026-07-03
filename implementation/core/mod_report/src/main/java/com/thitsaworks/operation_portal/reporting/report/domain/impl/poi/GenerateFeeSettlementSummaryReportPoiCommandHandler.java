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
package com.thitsaworks.operation_portal.reporting.report.domain.impl.poi;

import com.thitsaworks.operation_portal.component.misc.annotation.NoLogging;
import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeSettlementSummaryReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@NoLogging
public class GenerateFeeSettlementSummaryReportPoiCommandHandler implements GenerateFeeSettlementSummaryReportCommand {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateFeeSettlementSummaryReportPoiCommandHandler.class);

    private static final int DEFAULT_ROW_WINDOW = 200;

    private static final int REPORT_START_ROW = 0;

    private static final int REPORT_START_COLUMN = 0;

    private static final String[] SUMMARY_HEADERS = {
        "Sender DFSP",
        "Receiver DFSP",
        "Fee Policy",
        "Total Transactions",
        "Total Amount",
        "Total Fee",
        "Total Payer Fee",
        "Total Payee Fee",
        "Total Scheme Fee",
        "Currency"};

    private static final int[] SUMMARY_COLUMN_WIDTHS = {
        40,
        40,
        40,
        26,
        26,
        24,
        26,
        26,
        26,
        18};

    private static final String[] NET_SUMMARY_HEADERS = {
        "DFSP Name",
        "Settlement Amount",
        "Currency"};

    private final JdbcTemplate jdbcTemplate;

    public GenerateFeeSettlementSummaryReportPoiCommandHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws ReportException {

        try {
            if (!"xlsx".equalsIgnoreCase(input.fileType())) {
                throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
            }

            List<FeeSettlementSummaryRow> netSummarySourceRows = this.fetchFeeSettlementSummaryRows(input);
            if (netSummarySourceRows == null || netSummarySourceRows.isEmpty()) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            List<String> selectedParticipantNames = this.fetchSelectedParticipantNames(input.dfspId());
            boolean hideTransactionDetails = this.shouldHideTransactionDetails(input);
            List<FeeSettlementSummaryRow> transactionDetailRows = hideTransactionDetails ? List.of() :
                netSummarySourceRows;

            return new Output(
                this.exportXlsx(
                    input,
                    transactionDetailRows,
                    this.buildNetSummaryRows(
                        netSummarySourceRows, input.dfspId(), selectedParticipantNames)));

        } catch (ReportException exception) {
            throw exception;
        } catch (Exception exception) {
            LOG.error("Error generating fee settlement summary report", exception);
            throw new ReportException(ReportErrors.FEE_SETTLEMENT_SUMMARY_REPORT_FAILURE_EXCEPTION);
        }
    }

    @Override
    public Output exportAll(Input input, int totalRowCount, int pageSize) throws ReportException {

        if (totalRowCount <= 0) {
            throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
        }

        try {
            if (!"xlsx".equalsIgnoreCase(input.fileType())) {
                throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
            }

            List<FeeSettlementSummaryRow> netSummarySourceRows = this.fetchFeeSettlementSummaryRows(
                new Input(
                    input.settlementId(),
                    input.dfspId(),
                    input.timezone(),
                    input.fileType(),
                    input.loginDfspId(),
                    null,
                    null));
            if (netSummarySourceRows == null || netSummarySourceRows.isEmpty()) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            List<FeeSettlementSummaryRow> transactionDetailRows = new ArrayList<>();
            if (!this.shouldHideTransactionDetails(input)) {
                int normalizedPageSize = pageSize <= 0 ? totalRowCount : pageSize;
                int baseOffset = input.offset() == null ? 0 : input.offset();
                for (int offset = 0; offset < totalRowCount; offset += normalizedPageSize) {
                    int limit = Math.min(normalizedPageSize, totalRowCount - offset);
                    transactionDetailRows.addAll(this.fetchFeeSettlementSummaryRows(
                        new Input(
                            input.settlementId(),
                            input.dfspId(),
                            input.timezone(),
                            input.fileType(),
                            input.loginDfspId(),
                            baseOffset + offset,
                            limit)));
                }
            }

            return new Output(
                this.exportXlsx(
                    input,
                    transactionDetailRows,
                    this.buildNetSummaryRows(
                        netSummarySourceRows,
                        input.dfspId(),
                        this.fetchSelectedParticipantNames(input.dfspId()))));

        } catch (ReportException exception) {
            throw exception;
        } catch (Exception exception) {
            LOG.error("Error generating full fee settlement summary report", exception);
            throw new ReportException(ReportErrors.FEE_SETTLEMENT_SUMMARY_REPORT_FAILURE_EXCEPTION);
        }
    }

    @Override
    public int countRows(CountInput input) {

        Integer rowCount = this.jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM (" + this.feeSettlementSummaryQuery(false) + ") x",
            new Object[]{
                input.dfspId(),
                input.settlementId()
            },
            Integer.class);

        return rowCount == null ? 0 : rowCount;
    }

    private byte[] exportXlsx(Input input,
                              List<FeeSettlementSummaryRow> feeSettlementSummaryRows,
                              List<NetSummaryRow> netSummaryRows) throws IOException {

        Path tempFile = Files.createTempFile("fee-settlement-summary-", ".xlsx");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(DEFAULT_ROW_WINDOW);
             OutputStream outputStream = Files.newOutputStream(tempFile)) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("FeeSettlementSummaryReport");

            CellStyle metaLabelStyle = this.metaLabelStyle(workbook);
            CellStyle metaValueStyle = this.metaValueStyle(workbook);
            CellStyle columnHeaderStyle = this.columnHeaderStyle(workbook);
            CellStyle textCellStyle = this.textCellStyle(workbook);
            CellStyle integerCellStyle = this.integerCellStyle(workbook);
            CellStyle amountCellStyle = this.amountCellStyle(workbook);
            CellStyle sectionTitleStyle = this.sectionTitleStyle(workbook);
            CellStyle noteCellStyle = this.noteCellStyle(workbook);

            int rowIndex = REPORT_START_ROW;
            rowIndex = this.writeHeaderRow(
                sheet, rowIndex, "Settlement ID", input.settlementId(), metaLabelStyle,
                metaValueStyle);
            rowIndex = this.writeHeaderRow(
                sheet, rowIndex, "DFSP Name", input.dfspId(), metaLabelStyle, metaValueStyle);
            rowIndex++;

            int freezeRowIndex = rowIndex;
            if (!feeSettlementSummaryRows.isEmpty()) {
                Row columnHeaderRow = sheet.createRow(rowIndex++);
                for (int index = 0; index < SUMMARY_HEADERS.length; index++) {
                    Cell cell = columnHeaderRow.createCell(REPORT_START_COLUMN + index);
                    cell.setCellValue(SUMMARY_HEADERS[index]);
                    cell.setCellStyle(columnHeaderStyle);
                }
                freezeRowIndex = rowIndex;

                for (FeeSettlementSummaryRow feeSettlementSummaryRow : feeSettlementSummaryRows) {
                    this.writeSummaryDataRow(
                        sheet.createRow(rowIndex++), feeSettlementSummaryRow, textCellStyle, integerCellStyle,
                        amountCellStyle);
                }

                rowIndex++;
            }

            Row netSummaryTitleRow = sheet.createRow(rowIndex++);
            Cell netSummaryTitleCell = netSummaryTitleRow.createCell(REPORT_START_COLUMN);
            netSummaryTitleCell.setCellValue("Net Summary");
            netSummaryTitleCell.setCellStyle(sectionTitleStyle);

            Row netSummaryHeaderRow = sheet.createRow(rowIndex++);
            for (int index = 0; index < NET_SUMMARY_HEADERS.length; index++) {
                Cell cell = netSummaryHeaderRow.createCell(REPORT_START_COLUMN + index);
                cell.setCellValue(NET_SUMMARY_HEADERS[index]);
                cell.setCellStyle(columnHeaderStyle);
            }

            for (NetSummaryRow netSummaryRow : netSummaryRows) {
                Row row = sheet.createRow(rowIndex++);
                this.writeTextCell(row, REPORT_START_COLUMN, netSummaryRow.dfspName(), textCellStyle);
                this.writeAmountCell(
                    row, REPORT_START_COLUMN + 1, netSummaryRow.settlementAmount(), amountCellStyle);
                this.writeTextCell(row, REPORT_START_COLUMN + 2, netSummaryRow.currency(), textCellStyle);
            }

            rowIndex++;
            int noteStartRowIndex = rowIndex;
            Row noteRow = sheet.createRow(rowIndex++);
            Cell noteCell = noteRow.createCell(REPORT_START_COLUMN);
            noteCell.setCellValue("Note: +: Fund In / Credit, -:Fund Out / Debit");
            noteCell.setCellStyle(noteCellStyle);
            this.applyRangeBorder(
                sheet,
                noteStartRowIndex,
                noteStartRowIndex,
                REPORT_START_COLUMN,
                REPORT_START_COLUMN);

            for (int index = 0; index < SUMMARY_COLUMN_WIDTHS.length; index++) {
                sheet.setColumnWidth(index, SUMMARY_COLUMN_WIDTHS[index] * 256);
            }

            sheet.createFreezePane(0, freezeRowIndex);

            workbook.write(outputStream);
            workbook.dispose();
            return Files.readAllBytes(tempFile);

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private void writeSummaryDataRow(Row row,
                                     FeeSettlementSummaryRow data,
                                     CellStyle textCellStyle,
                                     CellStyle integerCellStyle,
                                     CellStyle amountCellStyle) {

        this.writeTextCell(row, REPORT_START_COLUMN, data.senderDfsp(), textCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 1, data.receiverDfsp(), textCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 2, data.feePolicy(), textCellStyle);
        this.writeIntegerCell(row, REPORT_START_COLUMN + 3, data.totalTransactions(), integerCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 4, data.totalAmount(), amountCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 5, data.totalFee(), amountCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 6, data.totalPayerFee(), amountCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 7, data.totalPayeeFee(), amountCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 8, data.totalSchemeFee(), amountCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 9, data.currency(), textCellStyle);
    }

    private List<NetSummaryRow> buildNetSummaryRows(List<FeeSettlementSummaryRow> feeSettlementSummaryRows,
                                                    String dfspId,
                                                    List<String> selectedParticipantNames) {

        Map<NetSummaryKey, BigDecimal> netSummary = new LinkedHashMap<>();
        boolean allDfsp = this.isAllDfsp(dfspId);
        Set<String> selectedParticipants = this.normalizedParticipantNames(selectedParticipantNames);

        for (FeeSettlementSummaryRow row : feeSettlementSummaryRows) {
            BigDecimal payerFee = this.valueOrZero(row.totalPayerFee());
            BigDecimal schemeFee = this.valueOrZero(row.totalSchemeFee());
            BigDecimal receiverSettlementAmount = payerFee.add(schemeFee).negate();

            if (allDfsp || this.containsParticipant(selectedParticipants, row.senderDfsp())) {
                this.addNetAmount(netSummary, row.senderDfsp(), row.currency(), payerFee);
            }

            if (allDfsp) {
                this.addNetAmount(netSummary, "Hub", row.currency(), schemeFee);
            }

            if (allDfsp || this.containsParticipant(selectedParticipants, row.receiverDfsp())) {
                this.addNetAmount(
                    netSummary, row.receiverDfsp(), row.currency(), receiverSettlementAmount);
            }
        }

        return netSummary.entrySet()
                         .stream()
                         .filter(entry -> entry.getValue().signum() != 0)
                         .map(entry -> new NetSummaryRow(
                             entry.getKey().dfspName(), entry.getValue(), entry.getKey().currency()))
                         .sorted(Comparator
                             .comparing(NetSummaryRow::dfspName, this::compareNetSummaryNames)
                             .thenComparing(NetSummaryRow::currency, String.CASE_INSENSITIVE_ORDER))
                         .toList();
    }

    private boolean shouldHideTransactionDetails(Input input) {

        return !this.isAllDfsp(input.dfspId()) &&
            !this.isHubDfsp(input.loginDfspId()) &&
            this.hasText(input.loginDfspId()) &&
            !input.loginDfspId().equalsIgnoreCase(input.dfspId());
    }

    private void addNetAmount(Map<NetSummaryKey, BigDecimal> netSummary,
                              String dfspName,
                              String currency,
                              BigDecimal amount) {

        if (!this.hasText(dfspName) || !this.hasText(currency) || amount == null ||
                amount.signum() == 0) {
            return;
        }

        netSummary.merge(new NetSummaryKey(dfspName, currency), amount, BigDecimal::add);
    }

    private List<FeeSettlementSummaryRow> fetchFeeSettlementSummaryRows(Input input) {

        if (input.limit() == null) {
            return this.jdbcTemplate.query(
                this.feeSettlementSummaryQuery(false),
                (rs, rowNum) -> this.mapFeeSettlementSummaryRow(rs),
                input.dfspId(),
                input.settlementId());
        }

        return this.jdbcTemplate.query(
            this.feeSettlementSummaryQuery(true),
            (rs, rowNum) -> this.mapFeeSettlementSummaryRow(rs),
            input.dfspId(),
            input.settlementId(),
            input.limit(),
            input.offset() == null ? 0 : input.offset());
    }

    private List<String> fetchSelectedParticipantNames(String dfspId) {

        if (this.isAllDfsp(dfspId)) {
            return List.of();
        }

        return List.of(dfspId);
    }

    private FeeSettlementSummaryRow mapFeeSettlementSummaryRow(ResultSet resultSet) throws SQLException {

        BigDecimal payerFee = resultSet.getBigDecimal("payerFee");
        BigDecimal payeeFee = resultSet.getBigDecimal("payeeFee");
        BigDecimal hubFee = resultSet.getBigDecimal("hubFee");

        return new FeeSettlementSummaryRow(
            resultSet.getString("payerDFSP"),
            resultSet.getString("payeeDFSP"),
            resultSet.getString("transactionSubScenario"),
            resultSet.getLong("totalTransactions"),
            resultSet.getBigDecimal("totalAmount"),
            this.valueOrZero(payerFee).add(this.valueOrZero(payeeFee)).add(this.valueOrZero(hubFee)),
            payerFee,
            payeeFee,
            hubFee,
            resultSet.getString("currency"));
    }

    private String feeSettlementSummaryQuery(boolean paged) {

        String query = """
            WITH report_filter AS (
              SELECT ? AS dfspId
            ),
            fee_per_quote AS (
              SELECT
                qe.quoteId,
                MAX(CASE WHEN qe.key = 'payerfee'  THEN CAST(qe.value AS DECIMAL(18,4)) END) AS payerFee,
                MAX(CASE WHEN qe.key = 'payeefee'  THEN CAST(qe.value AS DECIMAL(18,4)) END) AS payeeFee,
                MAX(CASE WHEN qe.key = 'schemeFee' THEN CAST(qe.value AS DECIMAL(18,4)) END) AS hubFee
              FROM quoteExtension qe
              GROUP BY qe.quoteId
            ),
            settlement_transfers AS (
              SELECT DISTINCT tf.transferId
              FROM settlement s
              JOIN settlementSettlementWindow ssw
                ON ssw.settlementId = s.settlementId
              JOIN transferFulfilment tf
                ON tf.settlementWindowId = ssw.settlementWindowId
              WHERE s.settlementId = ?
            )
            SELECT
              pPayer.name AS payerDFSP,
              pPayee.name AS payeeDFSP,
              q.currencyId AS currency,
              tss.name AS transactionSubScenario,
              COUNT(DISTINCT t.transferId) AS totalTransactions,
              SUM(t.amount) AS totalAmount,
              SUM(COALESCE(f.payerFee, 0)) AS payerFee,
              SUM(COALESCE(f.payeeFee, 0)) AS payeeFee,
              SUM(COALESCE(f.hubFee, 0)) AS hubFee
            FROM settlement_transfers st
            JOIN transfer t
              ON t.transferId = st.transferId
            JOIN transferParticipant tpPayer
              ON tpPayer.transferId = t.transferId
             AND tpPayer.transferParticipantRoleTypeId = (
                SELECT transferParticipantRoleTypeId
                FROM transferParticipantRoleType
                WHERE name = 'PAYER_DFSP'
             )
            JOIN participantCurrency pcPayer
              ON pcPayer.participantCurrencyId = tpPayer.participantCurrencyId
            JOIN participant pPayer
              ON pPayer.participantId = pcPayer.participantId
            JOIN transferParticipant tpPayee
              ON tpPayee.transferId = t.transferId
             AND tpPayee.transferParticipantRoleTypeId = (
                SELECT transferParticipantRoleTypeId
                FROM transferParticipantRoleType
                WHERE name = 'PAYEE_DFSP'
             )
            JOIN participantCurrency pcPayee
              ON pcPayee.participantCurrencyId = tpPayee.participantCurrencyId
            JOIN participant pPayee
              ON pPayee.participantId = pcPayee.participantId
            JOIN quote q
              ON q.transactionReferenceId = t.transferId
            LEFT JOIN transactionSubScenario tss
              ON tss.transactionSubScenarioId = q.transactionSubScenarioId
            LEFT JOIN fee_per_quote f
              ON f.quoteId = q.quoteId
            WHERE
              (SELECT dfspId FROM report_filter) = 'ALL'
              OR pPayer.name COLLATE utf8mb4_unicode_ci =
                 (SELECT dfspId FROM report_filter) COLLATE utf8mb4_unicode_ci
              OR pPayee.name COLLATE utf8mb4_unicode_ci =
                 (SELECT dfspId FROM report_filter) COLLATE utf8mb4_unicode_ci
            GROUP BY
              pPayer.name,
              pPayee.name,
              q.currencyId,
              tss.name
            ORDER BY
              pPayer.name,
              pPayee.name,
              q.currencyId,
              tss.name
            """;

        return paged ? query + " LIMIT ? OFFSET ?" : query;
    }

    private int writeHeaderRow(Sheet sheet,
                               int rowIndex,
                               String label,
                               String value,
                               CellStyle labelStyle,
                               CellStyle valueStyle) {

        Row row = sheet.createRow(rowIndex++);
        Cell labelCell = row.createCell(REPORT_START_COLUMN);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(REPORT_START_COLUMN + 1);
        valueCell.setCellValue(value == null ? "" : value);
        valueCell.setCellStyle(valueStyle);
        return rowIndex;
    }

    private void writeTextCell(Row row, int columnIndex, String value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void writeIntegerCell(Row row, int columnIndex, Long value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private void writeAmountCell(Row row, int columnIndex, BigDecimal value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private void applyStyleToRange(Sheet sheet,
                                   int firstRow,
                                   int lastRow,
                                   int firstColumn,
                                   int lastColumn,
                                   CellStyle style) {

        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    cell = row.createCell(columnIndex);
                }
                cell.setCellStyle(style);
            }
        }
    }

    private void applyRangeBorder(Sheet sheet,
                                  int firstRow,
                                  int lastRow,
                                  int firstColumn,
                                  int lastColumn) {

        CellRangeAddress range = new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn);
        RegionUtil.setBorderTop(BorderStyle.THIN, range, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, range, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, range, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, range, sheet);
    }

    private CellStyle metaLabelStyle(SXSSFWorkbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle metaValueStyle(SXSSFWorkbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.metaLabelStyle(workbook));
        style.setBorderLeft(BorderStyle.THIN);

        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(false);
        style.setFont(font);
        return style;
    }

    private CellStyle columnHeaderStyle(SXSSFWorkbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.metaLabelStyle(workbook));
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle sectionTitleStyle(SXSSFWorkbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.textCellStyle(workbook));
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle textCellStyle(org.apache.poi.ss.usermodel.Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        style.setFont(this.reportDataFont(workbook));
        return style;
    }

    private CellStyle noteCellStyle(org.apache.poi.ss.usermodel.Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.textCellStyle(workbook));
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle integerCellStyle(org.apache.poi.ss.usermodel.Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.textCellStyle(workbook));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private CellStyle amountCellStyle(org.apache.poi.ss.usermodel.Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.textCellStyle(workbook));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private org.apache.poi.ss.usermodel.Font reportDataFont(org.apache.poi.ss.usermodel.Workbook workbook) {

        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        return font;
    }

    private int compareNetSummaryNames(String left, String right) {

        boolean leftHub = "Hub".equalsIgnoreCase(left);
        boolean rightHub = "Hub".equalsIgnoreCase(right);
        if (leftHub && !rightHub) {
            return 1;
        }
        if (!leftHub && rightHub) {
            return -1;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(left, right);
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isAllDfsp(String dfspId) {

        return !this.hasText(dfspId) || "ALL".equalsIgnoreCase(dfspId);
    }

    private boolean isHubDfsp(String dfspId) {

        return "Hub".equalsIgnoreCase(dfspId);
    }

    private Set<String> normalizedParticipantNames(List<String> participantNames) {

        Set<String> normalizedNames = new HashSet<>();
        if (participantNames == null) {
            return normalizedNames;
        }

        for (String participantName : participantNames) {
            if (this.hasText(participantName)) {
                normalizedNames.add(participantName.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalizedNames;
    }

    private boolean containsParticipant(Set<String> participantNames, String participantName) {

        return this.hasText(participantName) &&
            participantNames.contains(participantName.trim().toLowerCase(Locale.ROOT));
    }

    private boolean hasText(String value) {

        return value != null && !value.isBlank();
    }

    private record FeeSettlementSummaryRow(String senderDfsp,
                                 String receiverDfsp,
                                 String feePolicy,
                                 Long totalTransactions,
                                 BigDecimal totalAmount,
                                 BigDecimal totalFee,
                                 BigDecimal totalPayerFee,
                                 BigDecimal totalPayeeFee,
                                 BigDecimal totalSchemeFee,
                                 String currency) { }

    private record NetSummaryKey(String dfspName,
                                 String currency) { }

    private record NetSummaryRow(String dfspName,
                                 BigDecimal settlementAmount,
                                 String currency) { }
}

