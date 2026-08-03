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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateRevenueSharingSummaryReportCommand;
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
import java.util.List;

@Service
@NoLogging
public class GenerateRevenueSharingSummaryReportPoiCommandHandler
    implements GenerateRevenueSharingSummaryReportCommand {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateRevenueSharingSummaryReportPoiCommandHandler.class);

    private static final int DEFAULT_ROW_WINDOW = 200;

    private static final String REPORT_QUERY = """
        SELECT '' AS responsibleMinistry, '' AS `type`, 0 AS balance, '' AS currency
        FROM tbl_transaction_detail
        """;

    private static final String[] COLUMN_HEADERS = {
        "Responsible Ministry",
        "Type",
        "Balance",
        "Currency"};

    private static final int[] COLUMN_WIDTHS = {
        40,
        34,
        30,
        22};

    private final JdbcTemplate jdbcTemplate;

    public GenerateRevenueSharingSummaryReportPoiCommandHandler(
        @Qualifier(PersistenceQualifiers.Core.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Output execute(Input input) throws ReportException {

        if (!"xlsx".equalsIgnoreCase(input.fileType())) {
            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
        }

        try {
            List<RevenueSharingSummaryRow> rows = this.fetchRows(input);
            if (rows.isEmpty()) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            return new Output(this.exportXlsx(input, rows));
        } catch (ReportException exception) {
            throw exception;
        } catch (Exception exception) {
            LOG.error("Error generating revenue sharing summary report", exception);
            throw new ReportException(
                ReportErrors.REVENUE_SHARING_SUMMARY_REPORT_FAILURE_EXCEPTION);
        }
    }

    @Override
    public int countRows() {

        Integer rowCount = this.jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM (" + REPORT_QUERY + ") revenueSharingSummary",
            Integer.class);
        return rowCount == null ? 0 : rowCount;
    }

    private List<RevenueSharingSummaryRow> fetchRows(Input input) {

        String query = REPORT_QUERY;
        Object[] parameters = new Object[0];
        if (input.limit() != null && input.offset() != null) {
            query += " LIMIT ? OFFSET ?";
            parameters = new Object[]{input.limit(), input.offset()};
        }

        return this.jdbcTemplate.query(
            query,
            (resultSet, rowNumber) -> new RevenueSharingSummaryRow(
                resultSet.getString("responsibleMinistry"),
                resultSet.getString("type"),
                resultSet.getBigDecimal("balance"),
                resultSet.getString("currency")),
            parameters);
    }

    private byte[] exportXlsx(Input input, List<RevenueSharingSummaryRow> rows)
        throws IOException {

        Path tempFile = Files.createTempFile("revenue-sharing-summary-", ".xlsx");
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(DEFAULT_ROW_WINDOW);
             OutputStream outputStream = Files.newOutputStream(tempFile)) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("RevenueSharingSummaryReport");

            CellStyle metaLabelStyle = this.metaLabelStyle(workbook);
            CellStyle metaValueStyle = this.metaValueStyle(workbook);
            CellStyle columnHeaderStyle = this.columnHeaderStyle(workbook);
            CellStyle textCellStyle = this.textCellStyle(workbook);
            CellStyle amountCellStyle = this.amountCellStyle(workbook);

            int rowIndex = 0;
            rowIndex = this.writeMeta(
                sheet, rowIndex, "Date", input.date(), metaLabelStyle, metaValueStyle);
            rowIndex = this.writeMeta(
                sheet, rowIndex, "Settlement ID", input.settlementId(), metaLabelStyle,
                metaValueStyle);
            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);
            for (int columnIndex = 0; columnIndex < COLUMN_HEADERS.length; columnIndex++) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(COLUMN_HEADERS[columnIndex]);
                cell.setCellStyle(columnHeaderStyle);
            }

            int freezeRowIndex = rowIndex;
            for (RevenueSharingSummaryRow data : rows) {
                Row row = sheet.createRow(rowIndex++);
                this.writeTextCell(row, 0, data.responsibleMinistry(), textCellStyle);
                this.writeTextCell(row, 1, data.type(), textCellStyle);
                this.writeAmountCell(row, 2, data.balance(), amountCellStyle);
                this.writeTextCell(row, 3, data.currency(), textCellStyle);
            }

            for (int columnIndex = 0; columnIndex < COLUMN_WIDTHS.length; columnIndex++) {
                sheet.setColumnWidth(columnIndex, COLUMN_WIDTHS[columnIndex] * 256);
            }
            sheet.createFreezePane(0, freezeRowIndex);

            workbook.write(outputStream);
            workbook.dispose();
            return Files.readAllBytes(tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private int writeMeta(Sheet sheet,
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
        valueCell.setCellValue(this.safe(value));
        valueCell.setCellStyle(valueStyle);
        return rowIndex;
    }

    private void writeTextCell(Row row, int columnIndex, String value, CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(this.safe(value));
        cell.setCellStyle(style);
    }

    private void writeAmountCell(Row row,
                                 int columnIndex,
                                 BigDecimal value,
                                 CellStyle style) {

        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? 0 : value.doubleValue());
        cell.setCellStyle(style);
    }

    private CellStyle metaLabelStyle(SXSSFWorkbook workbook) {

        CellStyle style = this.borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle metaValueStyle(SXSSFWorkbook workbook) {

        CellStyle style = this.borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }

    private CellStyle columnHeaderStyle(SXSSFWorkbook workbook) {

        CellStyle style = this.borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.TURQUOISE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle textCellStyle(SXSSFWorkbook workbook) {

        CellStyle style = this.borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        var font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }

    private CellStyle amountCellStyle(SXSSFWorkbook workbook) {

        CellStyle style = this.textCellStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle borderedStyle(SXSSFWorkbook workbook) {

        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String safe(String value) {

        return value == null ? "" : value;
    }

    private record RevenueSharingSummaryRow(String responsibleMinistry,
                                            String type,
                                            BigDecimal balance,
                                            String currency) { }
}
