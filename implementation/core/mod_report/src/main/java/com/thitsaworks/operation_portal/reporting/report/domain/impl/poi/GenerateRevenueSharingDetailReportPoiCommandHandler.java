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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateRevenueSharingDetailReportCommand;
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
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@NoLogging
public class GenerateRevenueSharingDetailReportPoiCommandHandler
    implements GenerateRevenueSharingDetailReportCommand {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateRevenueSharingDetailReportPoiCommandHandler.class);

    private static final int DEFAULT_ROW_WINDOW = 200;

    private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private static final int MYSQL_STREAM_FETCH_SIZE = Integer.MIN_VALUE;

    private static final BigDecimal PERCENT_DIVISOR = BigDecimal.valueOf(100);

    private static final String ALL_FILTER = "ALL";

    private static final String SHEET_NAME = "RevenueSharingDetailReport";

    private static final String[] COLUMN_HEADERS = {
        "Settlement ID",
        "HUB Transaction ID",
        "Receipt Number",
        "Bill Number (Invoice Number)",
        "Tax Code ID",
        "Tax Code ID (Description)",
        "Category",
        "Responsible Ministry",
        "3rd Party Name",
        "Sending DFSP Name",
        "Amount",
        "Currency",
        "GOL (%)",
        "GOL (Amount)",
        "Ministry (%)",
        "Ministry (Amount)",
        "Third Party (%)",
        "Third Party (Amount)",
        "Sending DFSP Commission (%)",
        "Sending DFSP Commission (Amount)"};

    private static final int[] COLUMN_WIDTHS = {
        18,
        34,
        22,
        28,
        18,
        38,
        18,
        30,
        30,
        26,
        18,
        14,
        14,
        18,
        16,
        20,
        18,
        22,
        28,
        32};

    private final JdbcTemplate jdbcTemplate;

    public GenerateRevenueSharingDetailReportPoiCommandHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws ReportException {

        String fileType = this.normalizeFileType(input.fileType());
        try {
            if ("xlsx".equalsIgnoreCase(fileType)) {
                return new Output(
                    this.exportXlsx(input, 0, input.limit() == null ? DEFAULT_LIMIT : input.limit()));
            }
            if ("csv".equalsIgnoreCase(fileType)) {
                return new Output(
                    this.exportCsv(input, 0, input.limit() == null ? DEFAULT_LIMIT : input.limit()));
            }

            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);

        } catch (ReportException exception) {
            throw exception;
        } catch (Exception exception) {
            LOG.error("Error generating revenue sharing detail report", exception);
            throw new ReportException(ReportErrors.REVENUE_SHARING_DETAIL_REPORT_FAILURE_EXCEPTION);
        }
    }

    @Override
    public Output exportAll(Input input, int totalRowCount, int pageSize) throws ReportException {

        if (totalRowCount <= 0) {
            throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
        }

        String fileType = this.normalizeFileType(input.fileType());
        try {
            if ("xlsx".equalsIgnoreCase(fileType)) {
                return new Output(this.exportXlsx(input, totalRowCount, pageSize));
            }
            if ("csv".equalsIgnoreCase(fileType)) {
                return new Output(this.exportCsv(input, totalRowCount, pageSize));
            }

            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);

        } catch (ReportException exception) {
            throw exception;

        } catch (Exception exception) {
            LOG.error("Error generating full revenue sharing detail report", exception);
            throw new ReportException(ReportErrors.REVENUE_SHARING_DETAIL_REPORT_FAILURE_EXCEPTION);
        }
    }

    @Override
    public int countRows(CountInput input) {

        Integer rowCount = this.jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*) AS rowCount
                FROM transferFulfilment tF
                JOIN settlementSettlementWindow sSW
                  ON sSW.settlementWindowId = tF.settlementWindowId
                JOIN operation_portal.tbl_transaction rt
                  ON rt.hub_transaction_id = tF.transferId
                JOIN operation_portal.tbl_transaction_detail rd
                  ON rd.transaction_id = rt.id
                WHERE tF.isValid
                  AND sSW.settlementId = ?
                  AND (? = 'ALL' OR rd.tax_code = ?)
                  AND (? = 'ALL' OR rd.category = ?)
                """,
            new Object[]{
                input.settlementId(),
                this.normalizeFilter(input.taxCode()),
                this.normalizeFilter(input.taxCode()),
                this.normalizeFilter(input.category()),
                this.normalizeFilter(input.category())},
            Integer.class);

        return rowCount == null ? 0 : rowCount;
    }

    private byte[] exportCsv(Input input, int totalRowCount, int pageSize)
        throws IOException, ReportException {

        Path tempFile = Files.createTempFile("revenue-sharing-detail-", ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write(this.csvLine("Settlement ID", this.displayValue(input.settlementId())));
            writer.write(this.csvLine(
                "Category", this.displayValue(this.normalizeFilter(input.category()))));
            writer.write(this.csvLine(
                "Tax Code", this.displayValue(this.normalizeFilter(input.taxCode()))));
            writer.write(this.csvLine("TimeZoneOffSet", this.displayValue(input.timezoneOffset())));
            writer.newLine();
            writer.write(this.csvLine(COLUMN_HEADERS));

            RowCounter rowCounter = new RowCounter();
            int baseOffset = input.offset() == null ? 0 : input.offset();
            int normalizedTotal = totalRowCount <= 0 ? DEFAULT_LIMIT : totalRowCount;
            int normalizedPageSize = pageSize <= 0 ? normalizedTotal : pageSize;
            for (int offset = 0; offset < normalizedTotal; offset += normalizedPageSize) {
                int limit = Math.min(normalizedPageSize, normalizedTotal - offset);
                Input chunkInput = new Input(
                    input.settlementId(), input.fileType(), input.timezoneOffset(),
                    input.taxCode(), input.category(),
                    baseOffset + offset, limit);

                this.streamRows(chunkInput, row -> {
                    writer.write(this.csvLine(this.toCsvValues(row)));
                    rowCounter.increment();
                });

                if (totalRowCount <= 0) {
                    break;
                }
            }

            if (rowCounter.value() == 0) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            writer.flush();
            return Files.readAllBytes(tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private byte[] exportXlsx(Input input, int totalRowCount, int pageSize)
        throws IOException, ReportException {

        Path tempFile = Files.createTempFile("revenue-sharing-detail-", ".xlsx");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(DEFAULT_ROW_WINDOW);
             OutputStream outputStream = Files.newOutputStream(tempFile)) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            CellStyle metaLabelStyle = this.metaLabelStyle(workbook);
            CellStyle metaValueStyle = this.metaValueStyle(workbook);
            CellStyle columnHeaderStyle = this.columnHeaderStyle(workbook);
            CellStyle textCellStyle = this.textCellStyle(workbook);
            CellStyle amountCellStyle = this.amountCellStyle(workbook);
            CellStyle percentCellStyle = this.percentCellStyle(workbook);

            int headerEndRow = this.writeReportHeader(sheet, input, metaLabelStyle, metaValueStyle);
            int rowIndex = headerEndRow + 1;
            Row columnHeaderRow = sheet.createRow(rowIndex++);
            for (int index = 0; index < COLUMN_HEADERS.length; index++) {
                Cell cell = columnHeaderRow.createCell(index);
                cell.setCellValue(COLUMN_HEADERS[index]);
                cell.setCellStyle(columnHeaderStyle);
            }

            RowCursor rowCursor = new RowCursor(rowIndex);
            int baseOffset = input.offset() == null ? 0 : input.offset();
            int normalizedTotal = totalRowCount <= 0 ? DEFAULT_LIMIT : totalRowCount;
            int normalizedPageSize = pageSize <= 0 ? normalizedTotal : pageSize;
            for (int offset = 0; offset < normalizedTotal; offset += normalizedPageSize) {
                int limit = Math.min(normalizedPageSize, normalizedTotal - offset);
                Input chunkInput = new Input(
                    input.settlementId(), input.fileType(), input.timezoneOffset(),
                    input.taxCode(), input.category(),
                    baseOffset + offset, limit);

                this.streamRows(
                    chunkInput, row -> this.writeDataRow(
                        sheet.createRow(rowCursor.next()), row, textCellStyle, amountCellStyle,
                        percentCellStyle));
                this.flushSheet(sheet);

                if (totalRowCount <= 0) {
                    break;
                }
            }

            if (rowCursor.current() == rowIndex) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            for (int index = 0; index < COLUMN_WIDTHS.length; index++) {
                sheet.setColumnWidth(index, COLUMN_WIDTHS[index] * 256);
            }

            sheet.createFreezePane(0, rowIndex);

            workbook.write(outputStream);
            workbook.dispose();
            return Files.readAllBytes(tempFile);

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private int writeReportHeader(Sheet sheet,
                                  Input input,
                                  CellStyle labelStyle,
                                  CellStyle valueStyle) {

        int rowIndex = 0;
        rowIndex = this.writeHeaderRow(
            sheet, rowIndex, "Settlement ID", this.displayValue(input.settlementId()), labelStyle,
            valueStyle);
        rowIndex = this.writeHeaderRow(
            sheet, rowIndex, "Category", this.displayValue(this.normalizeFilter(input.category())), labelStyle,
            valueStyle);
        rowIndex = this.writeHeaderRow(
            sheet, rowIndex, "Tax Code", this.displayValue(this.normalizeFilter(input.taxCode())), labelStyle,
            valueStyle);
        return this.writeHeaderRow(
            sheet, rowIndex, "TimeZoneOffSet", this.displayValue(input.timezoneOffset()), labelStyle,
            valueStyle);
    }

    private int writeHeaderRow(Sheet sheet,
                               int rowIndex,
                               String label,
                               String value,
                               CellStyle labelStyle,
                               CellStyle valueStyle) {

        Row row = sheet.createRow(rowIndex++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        return rowIndex;
    }

    private void streamRows(Input input, RevenueSharingDetailRowConsumer consumer) {

        QuerySpec querySpec = this.buildReportQuery();
        List<Object> parameters = new ArrayList<>(querySpec.params());
        parameters.add(input.settlementId());
        parameters.add(this.normalizeFilter(input.taxCode()));
        parameters.add(this.normalizeFilter(input.taxCode()));
        parameters.add(this.normalizeFilter(input.category()));
        parameters.add(this.normalizeFilter(input.category()));
        parameters.add(input.offset() == null ? 0 : input.offset());
        parameters.add(input.limit() == null ? DEFAULT_LIMIT : input.limit());

        try {
            this.jdbcTemplate.query(
                connection -> {
                    PreparedStatement statement = connection.prepareStatement(
                        querySpec.sql(),
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    statement.setFetchDirection(ResultSet.FETCH_FORWARD);
                    statement.setFetchSize(MYSQL_STREAM_FETCH_SIZE);
                    for (int index = 0; index < parameters.size(); index++) {
                        statement.setObject(index + 1, parameters.get(index));
                    }
                    return statement;
                }, resultSet -> {
                    try {
                        consumer.accept(this.mapRow(resultSet));
                    } catch (IOException exception) {
                        throw new IOExceptionRuntimeException(exception);
                    }
                });
        } catch (IOExceptionRuntimeException exception) {
            throw exception;
        }
    }

    private QuerySpec buildReportQuery() {

        return new QuerySpec(
            """
                SELECT
                  sSW.settlementId,
                  tF.transferId,
                  rt.receipt_number AS receiptNumber,
                  rt.bill_number AS billNumber,
                  rd.tax_code AS taxCode,
                  rd.tax_description AS taxDescription,
                  rd.category AS category,
                  CASE
                    WHEN rd.responsible_ministry_name IS NULL OR TRIM(rd.responsible_ministry_name) = ''
                      THEN '-'
                    ELSE rd.responsible_ministry_name
                  END AS responsibleMinistry,
                  CASE
                    WHEN rd.third_party_name IS NULL OR TRIM(rd.third_party_name) = ''
                      THEN '-'
                    ELSE rd.third_party_name
                  END AS thirdPartyName,
                  rt.sender_dfsp_id AS senderDfspName,
                  rd.calculated_amount AS amount,
                  rt.sent_currency AS currency,
                  rd.gol_percentage AS golPercentage,
                  rd.gol_amount AS golAmount,
                  rd.ministry_percent AS ministryPercentage,
                  rd.ministry_amount AS ministryAmount,
                  rd.third_party_percent AS thirdPartyPercentage,
                  rd.third_party_amount AS thirdPartyAmount,
                  rd.sending_dfsp_commission_percent AS sendingDfspCommissionPercentage,
                  rd.sending_dfsp_commission_amount AS sendingDfspCommissionAmount
                FROM transferFulfilment tF
                JOIN settlementSettlementWindow sSW
                  ON sSW.settlementWindowId = tF.settlementWindowId
                JOIN operation_portal.tbl_transaction rt
                  ON rt.hub_transaction_id = tF.transferId
                JOIN operation_portal.tbl_transaction_detail rd
                  ON rd.transaction_id = rt.id
                WHERE tF.isValid
                  AND sSW.settlementId = ?
                  AND (? = 'ALL' OR rd.tax_code = ?)
                  AND (? = 'ALL' OR rd.category = ?)
                ORDER BY
                  tF.transferId,
                  rd.tax_code
                LIMIT ?, ?
                """, List.of());
    }

    private RevenueSharingDetailRow mapRow(ResultSet resultSet) throws SQLException {

        return new RevenueSharingDetailRow(
            resultSet.getString("settlementId"), resultSet.getString("transferId"), resultSet.getString("receiptNumber"),
            resultSet.getString("billNumber"), resultSet.getString("taxCode"),
            resultSet.getString("taxDescription"), resultSet.getString("category"),
            resultSet.getString("responsibleMinistry"), resultSet.getString("thirdPartyName"),
            resultSet.getString("senderDfspName"), resultSet.getBigDecimal("amount"),
            resultSet.getString("currency"), resultSet.getBigDecimal("golPercentage"),
            resultSet.getBigDecimal("golAmount"), resultSet.getBigDecimal("ministryPercentage"),
            resultSet.getBigDecimal("ministryAmount"),
            resultSet.getBigDecimal("thirdPartyPercentage"),
            resultSet.getBigDecimal("thirdPartyAmount"),
            resultSet.getBigDecimal("sendingDfspCommissionPercentage"),
            resultSet.getBigDecimal("sendingDfspCommissionAmount"));
    }

    private void writeDataRow(Row row,
                              RevenueSharingDetailRow data,
                              CellStyle textCellStyle,
                              CellStyle amountCellStyle,
                              CellStyle percentCellStyle) {

        this.writeTextCell(row, 0, data.settlementId(), textCellStyle);
        this.writeTextCell(row, 1, data.hubTransactionId(), textCellStyle);
        this.writeTextCell(row, 2, data.receiptNumber(), textCellStyle);
        this.writeTextCell(row, 3, data.billNumber(), textCellStyle);
        this.writeTextCell(row, 4, data.taxCode(), textCellStyle);
        this.writeTextCell(row, 5, data.taxDescription(), textCellStyle);
        this.writeTextCell(row, 6, data.category(), textCellStyle);
        this.writeTextCell(row, 7, data.responsibleMinistry(), textCellStyle);
        this.writeTextCell(row, 8, data.thirdPartyName(), textCellStyle);
        this.writeTextCell(row, 9, data.senderDfspName(), textCellStyle);
        this.writeAmountCell(row, 10, data.amount(), amountCellStyle);
        this.writeTextCell(row, 11, data.currency(), textCellStyle);
        this.writePercentCell(row, 12, data.golPercentage(), percentCellStyle);
        this.writeAmountCell(row, 13, data.golAmount(), amountCellStyle);
        this.writePercentCell(row, 14, data.ministryPercentage(), percentCellStyle);
        this.writeAmountCell(row, 15, data.ministryAmount(), amountCellStyle);
        this.writePercentCell(row, 16, data.thirdPartyPercentage(), percentCellStyle);
        this.writeAmountOrDashCell(
            row, 17, data.thirdPartyAmount(), textCellStyle, amountCellStyle);
        this.writePercentCell(row, 18, data.sendingDfspCommissionPercentage(), percentCellStyle);
        this.writeAmountCell(row, 19, data.sendingDfspCommissionAmount(), amountCellStyle);
    }

    private void writeTextCell(Row row, int columnIndex, String value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : value);
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

    private void writePercentCell(Row row, int columnIndex, BigDecimal value, CellStyle style) {

        BigDecimal excelPercentValue = value == null ? null : value.divide(PERCENT_DIVISOR);
        this.writeAmountCell(row, columnIndex, excelPercentValue, style);
    }

    private void writeAmountOrDashCell(Row row,
                                       int columnIndex,
                                       BigDecimal value,
                                       CellStyle textStyle,
                                       CellStyle amountStyle) {

        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            this.writeTextCell(row, columnIndex, "-", textStyle);
            return;
        }

        this.writeAmountCell(row, columnIndex, value, amountStyle);
    }

    private String[] toCsvValues(RevenueSharingDetailRow row) {

        return new String[]{
            row.settlementId(),
            row.hubTransactionId(),
            row.receiptNumber(),
            row.billNumber(),
            row.taxCode(),
            row.taxDescription(),
            row.category(),
            row.responsibleMinistry(),
            row.thirdPartyName(),
            row.senderDfspName(),
            this.numberText(row.amount()),
            row.currency(),
            this.percentText(row.golPercentage()),
            this.numberText(row.golAmount()),
            this.percentText(row.ministryPercentage()),
            this.numberText(row.ministryAmount()),
            this.percentText(row.thirdPartyPercentage()),
            this.numberOrDashText(row.thirdPartyAmount()),
            this.percentText(row.sendingDfspCommissionPercentage()),
            this.numberText(row.sendingDfspCommissionAmount())};
    }

    private String numberText(BigDecimal value) {

        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String percentText(BigDecimal value) {

        String percentage = this.numberText(value);
        return percentage.isEmpty() ? "" : percentage + "%";
    }

    private String numberOrDashText(BigDecimal value) {

        return (value == null || BigDecimal.ZERO.compareTo(value) == 0) ? "-" : this.numberText(value);
    }

    private String csvLine(String... values) {

        StringBuilder line = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                line.append(',');
            }
            line.append(this.escapeCsv(values[index]));
        }
        line.append(System.lineSeparator());
        return line.toString();
    }

    private String escapeCsv(String value) {

        String safeValue = value == null ? "" : value;
        if (!safeValue.contains(",") && !safeValue.contains("\"") &&
                !safeValue.contains("\n") && !safeValue.contains("\r")) {
            return safeValue;
        }

        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private void flushSheet(Sheet sheet) throws IOException {

        if (sheet instanceof SXSSFSheet streamingSheet) {
            streamingSheet.flushRows(DEFAULT_ROW_WINDOW);
        }
    }

    private CellStyle metaLabelStyle(SXSSFWorkbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
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
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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

    private CellStyle amountCellStyle(org.apache.poi.ss.usermodel.Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.textCellStyle(workbook));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle percentCellStyle(org.apache.poi.ss.usermodel.Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(this.amountCellStyle(workbook));
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        return style;
    }

    private org.apache.poi.ss.usermodel.Font reportDataFont(org.apache.poi.ss.usermodel.Workbook workbook) {

        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        return font;
    }

    private String displayValue(String value) {

        return this.hasText(value) ? value.trim() : "-";
    }

    private String normalizeFileType(String fileType) {

        if (fileType == null) {
            return "";
        }

        String normalized = fileType.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }

    private String normalizeFilter(String value) {

        if (value == null || value.isBlank() || ALL_FILTER.equalsIgnoreCase(value.trim())) {
            return ALL_FILTER;
        }

        return value.trim();
    }

    private boolean hasText(String value) {

        return value != null && !value.isBlank();
    }

    private record QuerySpec(String sql, List<Object> params) { }

    private record RevenueSharingDetailRow(String settlementId,
                                           String hubTransactionId,
                                           String receiptNumber,
                                           String billNumber,
                                           String taxCode,
                                           String taxDescription,
                                           String category,
                                           String responsibleMinistry,
                                           String thirdPartyName,
                                           String senderDfspName,
                                           BigDecimal amount,
                                           String currency,
                                           BigDecimal golPercentage,
                                           BigDecimal golAmount,
                                           BigDecimal ministryPercentage,
                                           BigDecimal ministryAmount,
                                           BigDecimal thirdPartyPercentage,
                                           BigDecimal thirdPartyAmount,
                                           BigDecimal sendingDfspCommissionPercentage,
                                           BigDecimal sendingDfspCommissionAmount) { }

    @FunctionalInterface
    private interface RevenueSharingDetailRowConsumer {

        void accept(RevenueSharingDetailRow row) throws IOException;

    }

    private static final class IOExceptionRuntimeException extends RuntimeException {

        private IOExceptionRuntimeException(IOException cause) {

            super(cause);
        }

    }

    private static final class RowCursor {

        private int value;

        private RowCursor(int value) {

            this.value = value;
        }

        private int next() {

            return this.value++;
        }

        private int current() {

            return this.value;
        }

    }

    private static final class RowCounter {

        private int value;

        private void increment() {

            this.value++;
        }

        private int value() {

            return this.value;
        }

    }

}
