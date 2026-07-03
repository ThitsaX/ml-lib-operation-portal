package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.FileDownloadStatus;
import com.thitsaworks.operation_portal.component.common.type.ReportType;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.storage.S3FileStorage;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.query.UserQuery;
import com.thitsaworks.operation_portal.core.reporting.download.request.ReportDownloadRequestManager;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateFeeSettlementSummaryReport;
import com.thitsaworks.operation_portal.usecase.util.ReportDownloadUtil;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ActionMetadata(category = ActionCategory.REPORTING)
public class GenerateFeeSettlementSummaryReportHandler
    extends OperationPortalAuditableUseCase<GenerateFeeSettlementSummaryReport.Input, GenerateFeeSettlementSummaryReport.Output>
    implements GenerateFeeSettlementSummaryReport {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateFeeSettlementSummaryReportHandler.class);

    private static final String FILE_TYPE_XLSX = "xlsx";

    private final ReportDownloadRequestManager reportDownloadRequestManager;

    private final S3FileStorage s3FileStorage;

    private final UserQuery userQuery;

    public GenerateFeeSettlementSummaryReportHandler(CreateInputAuditCommand createInputAuditCommand,
                                                     CreateOutputAuditCommand createOutputAuditCommand,
                                                     CreateExceptionAuditCommand createExceptionAuditCommand,
                                                     ObjectMapper objectMapper,
                                                     PrincipalCache principalCache,
                                                     ActionAuthorizationManager actionAuthorizationManager,
                                                     ReportDownloadRequestManager reportDownloadRequestManager,
                                                     S3FileStorage s3FileStorage,
                                                     UserQuery userQuery) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.reportDownloadRequestManager = reportDownloadRequestManager;
        this.s3FileStorage = s3FileStorage;
        this.userQuery = userQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        Map<String, String> params = new HashMap<>();
        params.put("settlementId", input.settlementId());
        params.put("dfspId", this.normalizeAllToken(input.fspId()));
        params.put("timezoneOffset", input.timezone());
        params.put("loginDfspId", this.userQuery.get(new UserId(input.userId()))
                                                .participantName()
                                                .getValue());

        ReportDownloadRequestManager.CreateOrReuseResult result = this.reportDownloadRequestManager.createPendingOrReuse(
            ReportType.FEE_SETTLEMENT_SUMMARY, FILE_TYPE_XLSX, params);

        String fileKey = result.request().fileUrl();
        String fileUrl = null;

        if (FileDownloadStatus.READY.equals(result.request().status()) && fileKey != null &&
                !fileKey.isBlank()) {

            try {
                fileUrl = this.s3FileStorage.generatePreSignedDownloadUrl(fileKey);
            } catch (Exception e) {
                LOG.warn(
                    "Failed to generate pre-signed URL for requestId [{}]: [{}]",
                    result.request().requestId().getEntityId(), e.getMessage());
            }
        }

        if (FileDownloadStatus.FAILED.equals(result.request().status())) {
            throw new ReportException(
                ReportDownloadUtil.resolveFailedError(
                    result.request().errorMessage(),
                    ReportErrors.FEE_SETTLEMENT_SUMMARY_REPORT_FAILURE_EXCEPTION));
        }

        return new Output(
            result.request().requestId(), result.request().status(), fileUrl, fileKey,
            result.paramsSignature());
    }

    private String normalizeAllToken(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "ALL";
        }

        return "all".equalsIgnoreCase(value.trim()) ? "ALL" : value.trim();
    }

}
