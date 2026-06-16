package com.thitsaworks.operation_portal.reporting.report.domain.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import com.thitsaworks.operation_portal.reporting.report.ReportConfiguration;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeAmountSwiftReportCommand;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GenerateFeeAmountSwiftReportCommandHandler
    implements GenerateFeeAmountSwiftReportCommand {

    private static final String DEFAULT_SETTLEMENT_DATE = "000000";

    private static final String DEFAULT_CURRENCY = "XXX";

    private static final String DEFAULT_SENDER_BLOCK = "{1:NULL}";

    private static final String DEFAULT_SENDER_BLOCK_PARTICIPANT_ID = "1111111111111111";

    private static final String HUB_PARTICIPANT_NAME = "hub";

    private static final int MTID_MIN_LENGTH = 10;

    private final JdbcTemplate jdbcTemplate;

    private final ReportConfiguration.Settings reportSettings;

    @Autowired
    public GenerateFeeAmountSwiftReportCommandHandler(
        @Qualifier(PersistenceQualifiers.Hub.READ_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate,
        ReportConfiguration.Settings reportSettings) {

        this.jdbcTemplate = jdbcTemplate;
        this.reportSettings = reportSettings;
    }

    @Override
    public GenerateFeeAmountSwiftReportCommand.Output execute(GenerateFeeAmountSwiftReportCommand.Input input)
        throws ReportException {

        try {
            List<DirectionalFeeRow> feeRows = this.jdbcTemplate.query(
                """
                    WITH fee_per_quote AS (
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
                    ),
                    directional AS (
                      SELECT
                        pPayer.name AS payerDFSP,
                        pPayee.name AS payeeDFSP,
                        q.currencyId AS currency,
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
                      LEFT JOIN fee_per_quote f
                        ON f.quoteId = q.quoteId
                      GROUP BY
                        pPayer.name,
                        pPayee.name,
                        q.currencyId
                    )
                    SELECT
                      d.payerDFSP,
                      d.payeeDFSP,
                      d.currency,
                      d.totalTransactions,
                      d.totalAmount,
                      d.payerFee,
                      GREATEST(d.payeeFee - COALESCE(r.payeeFee, 0), 0) AS payeeFee,
                      d.hubFee
                    FROM directional d
                    LEFT JOIN directional r
                      ON r.payerDFSP = d.payeeDFSP
                     AND r.payeeDFSP = d.payerDFSP
                     AND r.currency = d.currency
                    ORDER BY
                      d.payerDFSP,
                      d.payeeDFSP,
                      d.currency;
                    """, (rs, rowNum) -> new DirectionalFeeRow(
                    rs.getString("payerDFSP"),
                    rs.getString("payeeDFSP"),
                    rs.getString("currency"),
                    rs.getBigDecimal("payerFee"),
                    rs.getBigDecimal("hubFee")),
                input.settlementId());

            List<SwiftParticipantFeeAmountRow> rows = this.buildSwiftParticipantFeeRows(feeRows, input);

            if (rows == null || rows.isEmpty()) {
                throw new ReportException(ReportErrors.RESULT_NOT_FOUND_EXCEPTION);
            }

            String senderBlock = this.resolveSenderBlock(input.settlementId());
            String swiftMessage = this.buildMt971Message(input.settlementId(), rows, senderBlock);
            return new Output(swiftMessage.getBytes(StandardCharsets.UTF_8));

        } catch (ReportException e) {
            throw e;
        } catch (Exception e) {
            throw new ReportException(ReportErrors.SETTLEMENT_BANK_REPORT_FAILURE_EXCEPTION);
        }
    }

    private List<SwiftParticipantFeeAmountRow> buildSwiftParticipantFeeRows(List<DirectionalFeeRow> feeRows, Input input) {

        if (feeRows == null || feeRows.isEmpty()) {
            return List.of();
        }

        String settlementDate = this.resolveSettlementDate(input.settlementId(), input.timezone());
        Map<ParticipantFeeAmountKey, SwiftParticipantFeeAmountRow> participantFeeAmounts = new LinkedHashMap<>();

        for (DirectionalFeeRow feeRow : feeRows) {
            if (!this.matchesCurrencyFilter(feeRow.currency(), input.currency())) {
                continue;
            }

            BigDecimal payerFee = this.valueOrZero(feeRow.payerFee());
            BigDecimal hubFee = this.valueOrZero(feeRow.hubFee());

            this.addParticipantFeeAmount(
                participantFeeAmounts,
                feeRow.payerDFSP(),
                feeRow.currency(),
                payerFee,
                settlementDate);
            this.addParticipantFeeAmount(
                participantFeeAmounts,
                HUB_PARTICIPANT_NAME,
                feeRow.currency(),
                hubFee,
                settlementDate);
            this.addParticipantFeeAmount(
                participantFeeAmounts,
                feeRow.payeeDFSP(),
                feeRow.currency(),
                payerFee.add(hubFee).negate(),
                settlementDate);
        }

        return participantFeeAmounts.values()
                                 .stream()
                                 .filter(row -> row.feeAmount() != null && row.feeAmount().signum() != 0)
                                 .sorted(Comparator.comparing(SwiftParticipantFeeAmountRow::accountNumber)
                                                   .thenComparing(SwiftParticipantFeeAmountRow::currencyId))
                                 .toList();
    }

    private void addParticipantFeeAmount(Map<ParticipantFeeAmountKey, SwiftParticipantFeeAmountRow> participantFeeAmounts,
                                         String participantName,
                                         String currencyId,
                                         BigDecimal feeAmount,
                                         String settlementDate) {

        if (!this.hasText(participantName) || feeAmount == null || feeAmount.signum() == 0) {
            return;
        }

        ParticipantSettlementProfile profile = this.resolveParticipantSettlementProfile(participantName, currencyId);
        ParticipantFeeAmountKey key = new ParticipantFeeAmountKey(
            profile.participantName(),
            currencyId,
            profile.accountNumber(),
            settlementDate);

        SwiftParticipantFeeAmountRow current = participantFeeAmounts.get(key);
        BigDecimal currentFeeAmount = current == null ? BigDecimal.ZERO : current.feeAmount();
        participantFeeAmounts.put(
            key,
            new SwiftParticipantFeeAmountRow(
                currencyId,
                currentFeeAmount.add(feeAmount),
                profile.accountNumber(),
                settlementDate));
    }

    private ParticipantSettlementProfile resolveParticipantSettlementProfile(String participantName, String currencyId) {

        List<ParticipantSettlementProfile> profiles = this.jdbcTemplate.query(
            """
                SELECT
                    COALESCE(op.parent_participant_name, op.participant_name, ?) AS participantName,
                    COALESCE(parent_lp.account_number, lp.account_number, '') AS accountNumber
                FROM operation_portal.tbl_participant op
                LEFT JOIN operation_portal.tbl_participant parent_op
                    ON parent_op.participant_name = op.parent_participant_name
                LEFT JOIN operation_portal.tbl_liquidity_profile lp
                    ON lp.participant_id = op.participant_id
                   AND lp.currency = ?
                   AND lp.is_active = 1
                LEFT JOIN operation_portal.tbl_liquidity_profile parent_lp
                    ON parent_lp.participant_id = parent_op.participant_id
                   AND parent_lp.currency = ?
                   AND parent_lp.is_active = 1
                WHERE op.participant_name = ?
                LIMIT 1
            """,
            (rs, rowNum) -> new ParticipantSettlementProfile(
                rs.getString("participantName"),
                rs.getString("accountNumber")),
            participantName,
            currencyId,
            currencyId,
            participantName);

        if (profiles == null || profiles.isEmpty()) {
            return new ParticipantSettlementProfile(participantName, "");
        }

        return profiles.get(0);
    }

    private String resolveSettlementDate(String settlementId, String timezone) {

        List<String> settlementDates = this.jdbcTemplate.query(
            """
                SELECT DATE_FORMAT(
                    CASE
                        WHEN SUBSTRING(?, 1, 1) = '-'
                            THEN CONVERT_TZ(
                                s.createdDate,
                                '+00:00',
                                CONCAT('-', SUBSTRING(?, 2, 2), ':', SUBSTRING(?, 4, 2))
                            )
                        ELSE CONVERT_TZ(
                            s.createdDate,
                            '+00:00',
                            CONCAT('+', SUBSTRING(?, 1, 2), ':', SUBSTRING(?, 3, 2))
                        )
                    END,
                    '%y%m%d'
                ) AS settlementDate
                FROM settlement s
                WHERE s.settlementId = ?
                LIMIT 1
                """,
            (rs, rowNum) -> rs.getString("settlementDate"),
            timezone,
            timezone,
            timezone,
            timezone,
            timezone,
            settlementId);

        if (settlementDates == null || settlementDates.isEmpty() || !this.hasText(settlementDates.get(0))) {
            return DEFAULT_SETTLEMENT_DATE;
        }

        return settlementDates.get(0);
    }

    private boolean matchesCurrencyFilter(String rowCurrency, String inputCurrency) {

        return !this.hasText(inputCurrency)
            || "ALL".equalsIgnoreCase(inputCurrency)
            || (this.hasText(rowCurrency) && rowCurrency.equalsIgnoreCase(inputCurrency));
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }

    private String buildMt971Message(String settlementId,
                                     List<SwiftParticipantFeeAmountRow> rows,
                                     String senderBlock) {

        String settlementDate = rows
                                    .stream()
                                    .map(SwiftParticipantFeeAmountRow::settlementDate)
                                    .filter(this::hasText)
                                    .findFirst()
                                    .orElse(DEFAULT_SETTLEMENT_DATE);

        String referenceNumber = settlementDate + "/" + this.calculateFeeMtid(settlementId);
        String receiverBic = this.reportSettings.receiverBIC();

        StringBuilder swift = new StringBuilder(512);
        swift.append(senderBlock)
             .append("\n");
        swift.append("{2:")
             .append(receiverBic)
             .append("}")
             .append("\n");
        swift.append("{3:{113:0010}{108:SETTL-FEE:/")
             .append(this.calculateFeeMtid(settlementId))
             .append("}}")
             .append("\n");
        swift.append("{4:")
             .append("\n");
        swift.append(":20:")
             .append(referenceNumber).append("\n");

        for (SwiftParticipantFeeAmountRow row : rows) {
            String currency = this.normalizeCurrency(row.currencyId());
            String dcMark = this.debitCreditMark(row.feeAmount());
            String feeAmount = this.toSwiftAmount(row.feeAmount());

            swift.append(":25:").append(row.accountNumber()).append("\n");
            swift
                .append(":62F:")
                .append(dcMark)
                .append(settlementDate)
                .append(currency)
                .append(feeAmount)
                .append(",")
                .append("\n");
        }

        swift.append("-}");
        return swift.toString();
    }

    private String resolveSenderBlock(String settlementId) {

        List<String> senderBlocks = this.jdbcTemplate.query(
            """
                SELECT account_number
                FROM operation_portal.tbl_liquidity_profile
                WHERE participant_id = ?
                LIMIT 1
                """, (rs, rowNum) -> rs.getString("account_number"),
            DEFAULT_SENDER_BLOCK_PARTICIPANT_ID);

        if (senderBlocks == null || senderBlocks.isEmpty()) {
            return DEFAULT_SENDER_BLOCK;
        }

        String senderBlock = senderBlocks.get(0);
        if (this.hasText(senderBlock)) {
            return "{1:" + senderBlock + this.calculateFeeMtid(settlementId) + "}";
        }
        return DEFAULT_SENDER_BLOCK;
    }

    private String calculateFeeMtid(String settlementId) {

        return this.formatMtid(new BigInteger(settlementId).multiply(BigInteger.TWO));
    }

    private String formatMtid(BigInteger mtid) {

        String value = mtid.toString();
        if (value.length() >= MTID_MIN_LENGTH) {
            return value;
        }
        return "0".repeat(MTID_MIN_LENGTH - value.length()) + value;
    }

    private String normalizeCurrency(String currencyId) {

        if (!this.hasText(currencyId)) {
            return DEFAULT_CURRENCY;
        }

        String normalized = currencyId.trim().toUpperCase(Locale.ROOT);
        return normalized.length() > 3 ? normalized.substring(0, 3) : normalized;
    }

    private String debitCreditMark(BigDecimal feeAmount) {

        if (feeAmount == null) {
            return "D";
        }
        return feeAmount.signum() < 0 ? "D" : "C";
    }

    private String toSwiftAmount(BigDecimal feeAmount) {

        BigDecimal value = feeAmount == null ? BigDecimal.ZERO : feeAmount.abs().stripTrailingZeros();
        String asPlain = value.toPlainString();
        return asPlain.replace('.', ',');
    }

    private boolean hasText(String value) {

        return value != null && !value.isBlank();
    }

    private record SwiftParticipantFeeAmountRow(String currencyId,
                                                BigDecimal feeAmount,
                                                String accountNumber,
                                                String settlementDate) { }

    private record DirectionalFeeRow(String payerDFSP,
                                     String payeeDFSP,
                                     String currency,
                                     BigDecimal payerFee,
                                     BigDecimal hubFee) { }

    private record ParticipantSettlementProfile(String participantName,
                                                String accountNumber) { }

    private record ParticipantFeeAmountKey(String participantName,
                                           String currencyId,
                                           String accountNumber,
                                           String settlementDate) { }

}
