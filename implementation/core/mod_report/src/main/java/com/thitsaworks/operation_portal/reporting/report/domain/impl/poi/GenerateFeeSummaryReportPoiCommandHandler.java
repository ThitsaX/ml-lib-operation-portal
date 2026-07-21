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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.thitsaworks.operation_portal.component.misc.annotation.NoLogging;
import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeSummaryReportCommand;
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
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@NoLogging
public class GenerateFeeSummaryReportPoiCommandHandler implements GenerateFeeSummaryReportCommand {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateFeeSummaryReportPoiCommandHandler.class);

    private static final int DEFAULT_ROW_WINDOW = 200;

    private static final int REPORT_START_ROW = 0;

    private static final int REPORT_START_COLUMN = 0;

    private static final String FILE_TYPE_XLSX = "xlsx";

    private static final String FILE_TYPE_PDF = "pdf";

    private static final String AMOUNT_FORMAT = "#,##0.00";

    private static final String COUNT_FORMAT = "#,##0";

    private static final DateTimeFormatter HEADER_DATE_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ssXXX");

    private static final String[] SUMMARY_HEADERS = {
        "Sender DFSP ID",
        "Sender DFSP Name",
        "Receiver DFSP ID",
        "Receiver DFSP Name",
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
        40,
        26,
        26,
        24,
        26,
        26,
        26,
        26,
        18};

    private static final float[] PDF_COLUMN_WIDTHS = {
        2.0f,
        2.2f,
        2.0f,
        2.4f,
        2.2f,
        1.8f,
        1.9f,
        1.6f,
        1.8f,
        1.8f,
        1.9f,
        1.2f};

    private static final float PDF_TOTAL_COLUMN_WIDTH = 22.8f;

    private static final float[] PDF_META_COLUMN_WIDTHS = {
        2.0f,
        2.2f};

    private static final float PDF_META_WIDTH_PERCENTAGE = (4.2f / PDF_TOTAL_COLUMN_WIDTH) * 100f;

    private static final float[] PDF_BALANCE_SUMMARY_COLUMN_WIDTHS = {
        2.0f,
        2.2f,
        2.0f,
        2.4f};

    private static final float PDF_BALANCE_SUMMARY_WIDTH_PERCENTAGE =
        (8.6f / PDF_TOTAL_COLUMN_WIDTH) * 100f;

    private static final String[] BALANCE_SUMMARY_HEADERS = {
        "DFSP Name",
        "Fund In",
        "Fund Out",
        "Currency"};

    private final JdbcTemplate jdbcTemplate;

    public GenerateFeeSummaryReportPoiCommandHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws ReportException {

        try {
            List<FeeSummaryRow> rows = this.fetchFeeSummaryRows(input);
            if (rows == null || rows.isEmpty()) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            if (FILE_TYPE_XLSX.equalsIgnoreCase(input.fileType())) {
                return new Output(this.exportXlsx(input, rows));
            }

            if (FILE_TYPE_PDF.equalsIgnoreCase(input.fileType())) {
                return new Output(this.exportPdf(input, rows));
            }

            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);

        } catch (ReportException exception) {
            throw exception;
        } catch (Exception exception) {
            LOG.error("Error generating fee summary report", exception);
            throw new ReportException(ReportErrors.FEE_SUMMARY_REPORT_FAILURE_EXCEPTION);
        }
    }

    @Override
    public Output exportAll(Input input, int totalRowCount, int pageSize) throws ReportException {

        return this.execute(new Input(
            input.startDate(),
            input.endDate(),
            input.dfspId(),
            input.timezone(),
            input.fileType(),
            input.loginDfspId(),
            input.offset(),
            input.limit()));
    }

    @Override
    public int countRows(CountInput input) {

        Integer rowCount = this.countFeeSummaryRows(
            input.startDate(), input.endDate(), input.dfspId(), input.timezone());
        LOG.info("Counted {} rows for fee summary report", rowCount);
        return rowCount == null ? 0 : rowCount;
    }

    private List<FeeSummaryRow> fetchFeeSummaryRows(Input input) {

        if (input.limit() == null) {
            return this.jdbcTemplate.query(
                this.feeSummaryQuery(false),
                (rs, rowNum) -> this.mapFeeSummaryRow(rs),
                this.queryParams(input));
        }

        return this.jdbcTemplate.query(
            this.feeSummaryQuery(true),
            (rs, rowNum) -> this.mapFeeSummaryRow(rs),
            this.queryParams(input));
    }

    private Integer countFeeSummaryRows(String startDate,
                                        String endDate,
                                        String dfspId,
                                        String timezone) {

        return this.jdbcTemplate.queryForObject(
            this.feeSummaryCountQuery(),
            this.queryParams(
                new Input(
                    startDate,
                    endDate,
                    dfspId,
                    timezone,
                    "xlsx",
                    null)),
            Integer.class);
    }

    private FeeSummaryRow mapFeeSummaryRow(ResultSet resultSet) throws SQLException {

        BigDecimal payerFee = resultSet.getBigDecimal("totalPayerFee");
        BigDecimal payeeFee = resultSet.getBigDecimal("totalPayeeFee");
        BigDecimal schemeFee = resultSet.getBigDecimal("totalSchemeFee");

        return new FeeSummaryRow(
            this.normalizeReportText(resultSet.getString("senderDFSPId")),
            this.normalizeReportText(resultSet.getString("senderDFSPName")),
            this.normalizeReportText(resultSet.getString("receiverDFSPId")),
            this.normalizeReportText(resultSet.getString("receiverDFSPName")),
            this.normalizeReportText(resultSet.getString("feePolicy")),
            resultSet.getLong("totalTransactions"),
            resultSet.getBigDecimal("totalAmount"),
            this.valueOrZero(payerFee).add(this.valueOrZero(payeeFee)).add(this.valueOrZero(schemeFee)),
            payerFee,
            payeeFee,
            schemeFee,
            resultSet.getString("currency"));
    }

    private String feeSummaryQuery(boolean paged) {

        String query = """
            WITH bounds_base AS (
              SELECT
                CASE WHEN SUBSTRING(?, 1, 1) = '-' THEN
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT(SUBSTRING(?, 1, 3), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                ELSE
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT('+', SUBSTRING(?, 2, 2), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                END AS startUtc,
                CASE WHEN SUBSTRING(?, 1, 1) = '-' THEN
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT(SUBSTRING(?, 1, 3), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                ELSE
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT('+', SUBSTRING(?, 2, 2), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                END AS endUtc
            ),
            transferList AS (
              SELECT t.transferId
              FROM transfer t
              INNER JOIN transferStateChange tsc
                ON t.transferId = tsc.transferId
               AND tsc.transferStateId = 'COMMITTED'
              CROSS JOIN bounds_base b
              WHERE tsc.createdDate BETWEEN b.startUtc AND b.endUtc
            )
            SELECT
              rs.payerFSP AS senderDFSPId,
              rs.payerFSPName AS senderDFSPName,
              rs.payeeFSP AS receiverDFSPId,
              rs.payeeFSPName AS receiverDFSPName,
              rs.feePolicy AS feePolicy,
              COUNT(DISTINCT rs.quoteId) AS totalTransactions,
              ROUND(SUM(rs.amount), 2) AS totalAmount,
              ROUND(SUM(
                COALESCE(rs.totalPayerFee, 0) +
                COALESCE(rs.totalPayeeFee, 0) +
                COALESCE(rs.totalSchemeFee, 0)
              ), 2) AS totalFee,
              ROUND(SUM(rs.totalPayerFee), 2) AS totalPayerFee,
              ROUND(SUM(rs.totalPayeeFee), 2) AS totalPayeeFee,
              ROUND(SUM(rs.totalSchemeFee), 2) AS totalSchemeFee,
              rs.currencyId AS currency
            FROM (
              SELECT
                q.quoteId,
                prp.name AS payerFSP,
                CASE
                  WHEN prp.description IS NULL OR CHAR_LENGTH(TRIM(prp.description)) = 0 THEN prp.name
                  ELSE TRIM(prp.description)
                END AS payerFSPName,
                pep.name AS payeeFSP,
                CASE
                  WHEN pep.description IS NULL OR CHAR_LENGTH(TRIM(pep.description)) = 0 THEN pep.name
                  ELSE TRIM(pep.description)
                END AS payeeFSPName,
                q.amount,
                MAX(CASE WHEN LOWER(qe.`key`) = 'feepolicytiername' THEN qe.value END) AS feePolicy,
                MAX(CASE WHEN LOWER(qe.`key`) = 'payerfee' THEN CAST(qe.value AS DECIMAL(18,2)) END) AS totalPayerFee,
                MAX(CASE WHEN LOWER(qe.`key`) = 'payeefee' THEN CAST(qe.value AS DECIMAL(18,2)) END) AS totalPayeeFee,
                MAX(CASE WHEN LOWER(qe.`key`) = 'schemefee' THEN CAST(qe.value AS DECIMAL(18,2)) END) AS totalSchemeFee,
                q.currencyId
              FROM quote q
              INNER JOIN quoteExtension qe
                ON q.quoteId = qe.quoteId
               AND LOWER(qe.`key`) IN ('payerfee', 'payeefee', 'schemefee', 'feepolicytiername')
              INNER JOIN quoteParty prqp
                ON q.quoteId = prqp.quoteId
               AND prqp.partyTypeId = (
                  SELECT pt.partyTypeId
                  FROM partyType pt
                  WHERE pt.name = 'PAYER'
               )
              INNER JOIN participant prp
                ON prqp.participantId = prp.participantId
              INNER JOIN quoteParty peqp
                ON q.quoteId = peqp.quoteId
               AND peqp.partyTypeId = (
                  SELECT pt.partyTypeId
                  FROM partyType pt
                  WHERE pt.name = 'PAYEE'
               )
              INNER JOIN participant pep
                ON peqp.participantId = pep.participantId
              INNER JOIN transferList tl
                ON tl.transferId = q.transactionReferenceId
              WHERE (
                  ? = 'ALL'
                  OR prp.name = ?
                  OR pep.name = ?
                )
              GROUP BY
                q.quoteId,
                payerFSP,
                payerFSPName,
                payeeFSP,
                payeeFSPName,
                q.amount,
                q.currencyId
            ) rs
            GROUP BY
              rs.payerFSP,
              rs.payerFSPName,
              rs.payeeFSP,
              rs.payeeFSPName,
              rs.feePolicy,
              rs.currencyId
            ORDER BY
              rs.payerFSP,
              rs.payeeFSP,
              rs.feePolicy,
              rs.currencyId
            """;

        return paged ? query + " LIMIT ? OFFSET ?" : query;
    }

    private String feeSummaryCountQuery() {

        return """
            WITH bounds_base AS (
              SELECT
                CASE WHEN SUBSTRING(?, 1, 1) = '-' THEN
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT(SUBSTRING(?, 1, 3), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                ELSE
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT('+', SUBSTRING(?, 2, 2), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                END AS startUtc,
                CASE WHEN SUBSTRING(?, 1, 1) = '-' THEN
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT(SUBSTRING(?, 1, 3), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                ELSE
                  CONVERT_TZ(
                    STR_TO_DATE(SUBSTRING(REPLACE(REPLACE(?, 'T', ' '), 'Z', ''), 1, 19), '%Y-%m-%d %H:%i:%s'),
                    CONCAT('+', SUBSTRING(?, 2, 2), ':', SUBSTRING(?, 4, 2)),
                    '+00:00')
                END AS endUtc
            ),
            transferList AS (
              SELECT t.transferId
              FROM transfer t
              INNER JOIN transferStateChange tsc
                ON t.transferId = tsc.transferId
               AND tsc.transferStateId = 'COMMITTED'
              CROSS JOIN bounds_base b
              WHERE tsc.createdDate BETWEEN b.startUtc AND b.endUtc
            )
            SELECT COUNT(*) FROM (
              SELECT
                rs.payerFSP,
                rs.payeeFSP,
                rs.feePolicy,
                rs.currencyId
              FROM (
                SELECT
                  q.quoteId,
                  prp.name AS payerFSP,
                  CASE
                    WHEN prp.description IS NULL OR CHAR_LENGTH(TRIM(prp.description)) = 0 THEN prp.name
                    ELSE TRIM(prp.description)
                  END AS payerFSPName,
                  pep.name AS payeeFSP,
                  CASE
                    WHEN pep.description IS NULL OR CHAR_LENGTH(TRIM(pep.description)) = 0 THEN pep.name
                    ELSE TRIM(pep.description)
                  END AS payeeFSPName,
                  q.amount,
                  MAX(CASE WHEN LOWER(qe.`key`) = 'feepolicytiername' THEN qe.value END) AS feePolicy,
                  q.currencyId
                FROM quote q
                INNER JOIN quoteExtension qe
                  ON q.quoteId = qe.quoteId
                 AND LOWER(qe.`key`) IN ('payerfee', 'payeefee', 'schemefee', 'feepolicytiername')
                INNER JOIN quoteParty prqp
                  ON q.quoteId = prqp.quoteId
                 AND prqp.partyTypeId = (
                    SELECT pt.partyTypeId
                    FROM partyType pt
                    WHERE pt.name = 'PAYER'
                 )
                INNER JOIN participant prp
                  ON prqp.participantId = prp.participantId
                INNER JOIN quoteParty peqp
                  ON q.quoteId = peqp.quoteId
                 AND peqp.partyTypeId = (
                    SELECT pt.partyTypeId
                    FROM partyType pt
                    WHERE pt.name = 'PAYEE'
                 )
                INNER JOIN participant pep
                  ON peqp.participantId = pep.participantId
                INNER JOIN transferList tl
                  ON tl.transferId = q.transactionReferenceId
                WHERE
                  (
                    ? = 'ALL'
                    OR prp.name = ?
                    OR pep.name = ?
                  )
                GROUP BY
                  q.quoteId,
                  payerFSP,
                  payerFSPName,
                  payeeFSP,
                  payeeFSPName,
                  q.amount,
                  q.currencyId
              ) rs
              GROUP BY
                rs.payerFSP,
                rs.payerFSPName,
                rs.payeeFSP,
                rs.payeeFSPName,
                rs.currencyId,
                rs.feePolicy
            ) x
            """;
    }

    private Object[] queryParams(Input input) {

        String dfspId = this.normalizeAllToken(input.dfspId());
        String timezone = this.normalizeTimezone(input.timezone());
        Object[] baseParams = {
            timezone,
            input.startDate(),
            timezone,
            timezone,
            input.startDate(),
            timezone,
            timezone,
            timezone,
            input.endDate(),
            timezone,
            timezone,
            input.endDate(),
            timezone,
            timezone,
            dfspId,
            dfspId,
            dfspId
        };

        if (input.limit() == null) {
            return baseParams;
        }

        Object[] pagedParams = new Object[baseParams.length + 2];
        System.arraycopy(baseParams, 0, pagedParams, 0, baseParams.length);
        pagedParams[baseParams.length] = input.limit();
        pagedParams[baseParams.length + 1] = input.offset() == null ? 0 : input.offset();
        return pagedParams;
    }

    private byte[] exportXlsx(Input input, List<FeeSummaryRow> rows) throws IOException {

        Path tempFile = Files.createTempFile("fee-summary-", ".xlsx");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(DEFAULT_ROW_WINDOW);
             OutputStream outputStream = Files.newOutputStream(tempFile)) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("FeeSummaryReport");

            CellStyle metaLabelStyle = this.metaLabelStyle(workbook);
            CellStyle metaValueStyle = this.metaValueStyle(workbook);
            CellStyle columnHeaderStyle = this.columnHeaderStyle(workbook);
            CellStyle textCellStyle = this.textCellStyle(workbook);
            CellStyle integerCellStyle = this.integerCellStyle(workbook);
            CellStyle amountCellStyle = this.amountCellStyle(workbook);
            CellStyle sectionTitleStyle = this.sectionTitleStyle(workbook);

            String filterDfspName = this.formatFilterDfspName(input.dfspId(), rows);
            int rowIndex = REPORT_START_ROW;
            rowIndex = this.writeHeaderRow(
                sheet, rowIndex, "Start Date", this.formatHeaderDate(input.startDate(), input.timezone()),
                metaLabelStyle, metaValueStyle);
            rowIndex = this.writeHeaderRow(
                sheet, rowIndex, "End Date", this.formatHeaderDate(input.endDate(), input.timezone()),
                metaLabelStyle, metaValueStyle);
            rowIndex = this.writeHeaderRow(
                sheet, rowIndex, "DFSP Name", filterDfspName, metaLabelStyle, metaValueStyle);
            rowIndex++;

            Row columnHeaderRow = sheet.createRow(rowIndex++);
            for (int index = 0; index < SUMMARY_HEADERS.length; index++) {
                Cell cell = columnHeaderRow.createCell(REPORT_START_COLUMN + index);
                cell.setCellValue(SUMMARY_HEADERS[index]);
                cell.setCellStyle(columnHeaderStyle);
            }

            int freezeRowIndex = rowIndex;
            for (FeeSummaryRow rowData : rows) {
                this.writeSummaryDataRow(
                    sheet.createRow(rowIndex++), rowData, textCellStyle, integerCellStyle, amountCellStyle);
            }

            rowIndex++;
            Row summaryTitleRow = sheet.createRow(rowIndex++);
            Cell summaryTitleCell = summaryTitleRow.createCell(REPORT_START_COLUMN);
            summaryTitleCell.setCellValue("Summary");
            summaryTitleCell.setCellStyle(sectionTitleStyle);

            Row balanceHeaderRow = sheet.createRow(rowIndex++);
            for (int index = 0; index < BALANCE_SUMMARY_HEADERS.length; index++) {
                Cell cell = balanceHeaderRow.createCell(REPORT_START_COLUMN + index);
                cell.setCellValue(BALANCE_SUMMARY_HEADERS[index]);
                cell.setCellStyle(columnHeaderStyle);
            }

            for (BalanceSummaryRow balanceSummaryRow : this.buildBalanceSummaryRows(rows)) {
                this.writeBalanceSummaryRow(
                    sheet.createRow(rowIndex++), balanceSummaryRow, textCellStyle, amountCellStyle);
            }

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

    private byte[] exportPdf(Input input, List<FeeSummaryRow> rows)
        throws IOException, DocumentException {

        Path tempFile = Files.createTempFile("fee-summary-", ".pdf");

        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
            Document document = new Document(PageSize.A3.rotate(), 18, 18, 18, 18);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 8);
            String filterDfspName = this.formatFilterDfspName(input.dfspId(), rows);

            PdfPTable metaTable = new PdfPTable(PDF_META_COLUMN_WIDTHS);
            metaTable.setWidthPercentage(PDF_META_WIDTH_PERCENTAGE);
            metaTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            this.addPdfMetaRow(
                metaTable, "Start Date", this.formatHeaderDate(input.startDate(), input.timezone()),
                labelFont, normalFont);
            this.addPdfMetaRow(
                metaTable, "End Date", this.formatHeaderDate(input.endDate(), input.timezone()), labelFont,
                normalFont);
            this.addPdfMetaRow(
                metaTable, "DFSP Name", filterDfspName, labelFont, normalFont);
            metaTable.setSpacingAfter(10f);
            document.add(metaTable);

            PdfPTable detailTable = new PdfPTable(PDF_COLUMN_WIDTHS);
            detailTable.setWidthPercentage(100f);
            for (String header : SUMMARY_HEADERS) {
                detailTable.addCell(this.pdfCell(header, labelFont, Element.ALIGN_LEFT));
            }

            for (FeeSummaryRow row : rows) {
                detailTable.addCell(this.pdfCell(row.senderDfspId(), normalFont, Element.ALIGN_LEFT));
                detailTable.addCell(this.pdfCell(row.senderDfspName(), normalFont, Element.ALIGN_LEFT));
                detailTable.addCell(this.pdfCell(row.receiverDfspId(), normalFont, Element.ALIGN_LEFT));
                detailTable.addCell(this.pdfCell(row.receiverDfspName(), normalFont, Element.ALIGN_LEFT));
                detailTable.addCell(this.pdfCell(row.feePolicy(), normalFont, Element.ALIGN_LEFT));
                detailTable.addCell(this.pdfCell(this.formatCount(row.totalTransactions()), normalFont, Element.ALIGN_RIGHT));
                detailTable.addCell(this.pdfCell(this.formatAmount(row.totalAmount()), normalFont, Element.ALIGN_RIGHT));
                detailTable.addCell(this.pdfCell(this.formatAmount(row.totalFee()), normalFont, Element.ALIGN_RIGHT));
                detailTable.addCell(this.pdfCell(this.formatOptionalAmount(row.totalPayerFee()), normalFont, Element.ALIGN_RIGHT));
                detailTable.addCell(this.pdfCell(this.formatOptionalAmount(row.totalPayeeFee()), normalFont, Element.ALIGN_RIGHT));
                detailTable.addCell(this.pdfCell(this.formatOptionalAmount(row.totalSchemeFee()), normalFont, Element.ALIGN_RIGHT));
                detailTable.addCell(this.pdfCell(row.currency(), normalFont, Element.ALIGN_LEFT));
            }
            detailTable.setSpacingAfter(10f);
            document.add(detailTable);

            PdfPTable summaryTable = new PdfPTable(PDF_BALANCE_SUMMARY_COLUMN_WIDTHS);
            summaryTable.setWidthPercentage(PDF_BALANCE_SUMMARY_WIDTH_PERCENTAGE);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.addCell(this.pdfCell("Summary", labelFont, Element.ALIGN_LEFT, 4));
            summaryTable.addCell(this.pdfCell(BALANCE_SUMMARY_HEADERS[0], labelFont, Element.ALIGN_LEFT));
            summaryTable.addCell(this.pdfCell(BALANCE_SUMMARY_HEADERS[1], labelFont, Element.ALIGN_LEFT));
            summaryTable.addCell(this.pdfCell(BALANCE_SUMMARY_HEADERS[2], labelFont, Element.ALIGN_LEFT));
            summaryTable.addCell(this.pdfCell(BALANCE_SUMMARY_HEADERS[3], labelFont, Element.ALIGN_LEFT));

            for (BalanceSummaryRow row : this.buildBalanceSummaryRows(rows)) {
                summaryTable.addCell(this.pdfCell(row.dfspName(), normalFont, Element.ALIGN_LEFT));
                summaryTable.addCell(
                    this.pdfCell(this.formatBalanceAmount(row.fundIn()), normalFont, Element.ALIGN_RIGHT));
                summaryTable.addCell(
                    this.pdfCell(this.formatBalanceAmount(row.fundOut()), normalFont, Element.ALIGN_RIGHT));
                summaryTable.addCell(this.pdfCell(row.currency(), normalFont, Element.ALIGN_LEFT));
            }
            document.add(summaryTable);

            document.close();
            return Files.readAllBytes(tempFile);

        } finally {
            Files.deleteIfExists(tempFile);
        }
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

    private void writeSummaryDataRow(Row row,
                                     FeeSummaryRow data,
                                     CellStyle textCellStyle,
                                     CellStyle integerCellStyle,
                                     CellStyle amountCellStyle) {

        this.writeTextCell(row, REPORT_START_COLUMN, data.senderDfspId(), textCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 1, data.senderDfspName(), textCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 2, data.receiverDfspId(), textCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 3, data.receiverDfspName(), textCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 4, data.feePolicy(), textCellStyle);
        this.writeIntegerCell(row, REPORT_START_COLUMN + 5, data.totalTransactions(), integerCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 6, data.totalAmount(), amountCellStyle);
        this.writeAmountCell(row, REPORT_START_COLUMN + 7, data.totalFee(), amountCellStyle);
        this.writeOptionalAmountCell(row, REPORT_START_COLUMN + 8, data.totalPayerFee(), amountCellStyle);
        this.writeOptionalAmountCell(row, REPORT_START_COLUMN + 9, data.totalPayeeFee(), amountCellStyle);
        this.writeOptionalAmountCell(row, REPORT_START_COLUMN + 10, data.totalSchemeFee(), amountCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 11, data.currency(), textCellStyle);
    }

    private void writeBalanceSummaryRow(Row row,
                                        BalanceSummaryRow data,
                                        CellStyle textCellStyle,
                                        CellStyle amountCellStyle) {

        this.writeTextCell(row, REPORT_START_COLUMN, data.dfspName(), textCellStyle);
        this.writeBalanceAmountCell(row, REPORT_START_COLUMN + 1, data.fundIn(), amountCellStyle);
        this.writeBalanceAmountCell(row, REPORT_START_COLUMN + 2, data.fundOut(), amountCellStyle);
        this.writeTextCell(row, REPORT_START_COLUMN + 3, data.currency(), textCellStyle);
    }

    private List<BalanceSummaryRow> buildBalanceSummaryRows(List<FeeSummaryRow> rows) {

        Map<BalanceSummaryKey, BigDecimal> balances = new LinkedHashMap<>();
        for (FeeSummaryRow row : rows) {
            BigDecimal payeeFee = this.valueOrZero(row.totalPayeeFee());
            BigDecimal schemeFee = this.valueOrZero(row.totalSchemeFee());
            BigDecimal payerFundOut = payeeFee.add(schemeFee);

            this.addBalanceAmount(
                balances, row.senderDfspId(), row.senderDfspName(), row.currency(), payerFundOut.negate());
            this.addBalanceAmount(
                balances, row.receiverDfspId(), row.receiverDfspName(), row.currency(), payeeFee);
            this.addBalanceAmount(balances, "Scheme", "Scheme", row.currency(), schemeFee);
        }

        return balances.entrySet()
                       .stream()
                       .filter(entry -> entry.getValue().signum() != 0)
                       .map(entry -> this.toBalanceSummaryRow(entry.getKey(), entry.getValue()))
                       .sorted(Comparator
                           .comparing(BalanceSummaryRow::dfspName, this::compareSummaryNames)
                           .thenComparing(BalanceSummaryRow::currency, String.CASE_INSENSITIVE_ORDER))
                       .toList();
    }

    private void addBalanceAmount(Map<BalanceSummaryKey, BigDecimal> balances,
                                  String dfspId,
                                  String dfspName,
                                  String currency,
                                  BigDecimal amount) {

        if (dfspId == null || dfspId.isBlank() || dfspName == null || dfspName.isBlank() ||
                currency == null || currency.isBlank() ||
                amount == null || amount.signum() == 0) {
            return;
        }

        balances.merge(new BalanceSummaryKey(dfspId, dfspName, currency), amount, BigDecimal::add);
    }

    private BalanceSummaryRow toBalanceSummaryRow(BalanceSummaryKey key, BigDecimal balance) {

        if (balance.signum() > 0) {
            return new BalanceSummaryRow(key.dfspName(), balance, null, key.currency());
        }

        return new BalanceSummaryRow(key.dfspName(), null, balance.abs(), key.currency());
    }

    private int compareSummaryNames(String left, String right) {

        boolean leftHub = "Scheme".equalsIgnoreCase(left);
        boolean rightHub = "Scheme".equalsIgnoreCase(right);
        if (leftHub && !rightHub) {
            return 1;
        }
        if (!leftHub && rightHub) {
            return -1;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(left, right);
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

    private void writeOptionalAmountCell(Row row, int columnIndex, BigDecimal value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        if (this.hasNonZeroAmount(value)) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("-");
        }
        cell.setCellStyle(style);
    }

    private void writeBalanceAmountCell(Row row, int columnIndex, BigDecimal value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("-");
        }
        cell.setCellStyle(style);
    }

    private PdfPCell pdfCell(String text, Font font, int horizontalAlignment) {

        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell pdfCell(String text, Font font, int horizontalAlignment, int colspan) {

        PdfPCell cell = this.pdfCell(text, font, horizontalAlignment);
        cell.setColspan(colspan);
        return cell;
    }

    private void addPdfMetaRow(PdfPTable table,
                               String label,
                               String value,
                               Font labelFont,
                               Font valueFont) {

        table.addCell(this.pdfCell(label, labelFont, Element.ALIGN_LEFT));
        table.addCell(this.pdfCell(value, valueFont, Element.ALIGN_LEFT));
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

    private String normalizeAllToken(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "ALL";
        }

        return "all".equalsIgnoreCase(value.trim()) ? "ALL" : value.trim();
    }

    private String formatFilterDfspName(String dfspId, List<FeeSummaryRow> rows) {

        String normalizedDfspId = this.normalizeAllToken(dfspId);
        if ("ALL".equalsIgnoreCase(normalizedDfspId) || rows == null) {
            return normalizedDfspId;
        }

        for (FeeSummaryRow row : rows) {
            String displayName = this.matchingDfspName(normalizedDfspId, row);
            if (displayName != null) {
                return displayName;
            }
        }

        return normalizedDfspId;
    }

    private String matchingDfspName(String dfspId, FeeSummaryRow row) {

        if (row == null) {
            return null;
        }

        if (dfspId.equalsIgnoreCase(row.senderDfspId())) {
            return this.formatDfspIdAndName(row.senderDfspId(), row.senderDfspName());
        }

        if (dfspId.equalsIgnoreCase(row.receiverDfspId())) {
            return this.formatDfspIdAndName(row.receiverDfspId(), row.receiverDfspName());
        }

        return null;
    }

    private String formatDfspIdAndName(String dfspId, String dfspName) {

        if (dfspId == null || dfspId.isBlank()) {
            return "";
        }

        if (dfspName == null || dfspName.isBlank() || dfspId.equalsIgnoreCase(dfspName.trim())) {
            return dfspId;
        }

        return dfspId + " (" + dfspName.trim() + ")";
    }

    private String normalizeReportText(String value) {

        if (value == null) {
            return null;
        }

        return value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
    }

    private String formatCount(Long value) {

        return value == null ? "" : new DecimalFormat(COUNT_FORMAT).format(value);
    }

    private String formatAmount(BigDecimal value) {

        return value == null ? "" : new DecimalFormat(AMOUNT_FORMAT).format(value);
    }

    private String formatOptionalAmount(BigDecimal value) {

        return this.hasNonZeroAmount(value) ? new DecimalFormat(AMOUNT_FORMAT).format(value) : "-";
    }

    private String formatBalanceAmount(BigDecimal value) {

        return value == null ? "-" : new DecimalFormat(AMOUNT_FORMAT).format(value);
    }

    private String normalizeTimezone(String value) {

        if (value == null || value.isBlank() || "0000".equals(value.trim())) {
            return "+0000";
        }

        String trimmedValue = value.trim();
        return trimmedValue.startsWith("+") || trimmedValue.startsWith("-") ? trimmedValue : "+" + trimmedValue;
    }

    private String formatHeaderDate(String value, String rawOffset) {

        if (value == null || value.isBlank()) {
            return "";
        }

        ZoneOffset zoneOffset = this.parseOffset(rawOffset);
        String trimmedValue = value.trim();

        try {
            return Instant.parse(trimmedValue)
                          .atOffset(ZoneOffset.UTC)
                          .withOffsetSameLocal(zoneOffset)
                          .format(HEADER_DATE_FORMAT)
                          .replace("Z", "+00:00");
        } catch (DateTimeParseException exception) {
            try {
                return LocalDateTime.parse(trimmedValue.replace("Z", ""))
                                    .atOffset(zoneOffset)
                                    .format(HEADER_DATE_FORMAT)
                                    .replace("Z", "+00:00");
            } catch (DateTimeParseException ignored) {
                return trimmedValue;
            }
        }
    }

    private ZoneOffset parseOffset(String rawOffset) {

        if (rawOffset == null || rawOffset.isBlank()) {
            return ZoneOffset.UTC;
        }

        String normalized = rawOffset.trim();
        if (normalized.matches("[+-]\\d{4}")) {
            normalized = normalized.substring(0, 3) + ":" + normalized.substring(3);
        } else if (normalized.matches("\\d{4}")) {
            normalized = "+" + normalized.substring(0, 2) + ":" + normalized.substring(2);
        }

        return ZoneOffset.of(normalized);
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean hasNonZeroAmount(BigDecimal value) {

        return value != null && value.signum() != 0;
    }

    private record FeeSummaryRow(String senderDfspId,
                                 String senderDfspName,
                                 String receiverDfspId,
                                 String receiverDfspName,
                                 String feePolicy,
                                 Long totalTransactions,
                                 BigDecimal totalAmount,
                                 BigDecimal totalFee,
                                 BigDecimal totalPayerFee,
                                 BigDecimal totalPayeeFee,
                                 BigDecimal totalSchemeFee,
                                 String currency) { }

    private record BalanceSummaryKey(String dfspId,
                                     String dfspName,
                                     String currency) { }

    private record BalanceSummaryRow(String dfspName,
                                     BigDecimal fundIn,
                                     BigDecimal fundOut,
                                     String currency) { }
}
