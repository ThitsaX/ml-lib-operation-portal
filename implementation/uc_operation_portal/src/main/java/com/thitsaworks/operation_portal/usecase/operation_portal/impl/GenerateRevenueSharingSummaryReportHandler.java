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
package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.thitsaworks.operation_portal.core.reporting.download.request.ReportDownloadRequestManager;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportErrors;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateRevenueSharingSummaryReport;
import com.thitsaworks.operation_portal.usecase.util.ReportDownloadUtil;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ActionMetadata(category = ActionCategory.REPORTING)
public class GenerateRevenueSharingSummaryReportHandler
    extends OperationPortalAuditableUseCase<GenerateRevenueSharingSummaryReport.Input,
                                               GenerateRevenueSharingSummaryReport.Output>
    implements GenerateRevenueSharingSummaryReport {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateRevenueSharingSummaryReportHandler.class);

    private final ReportDownloadRequestManager reportDownloadRequestManager;

    private final S3FileStorage s3FileStorage;

    public GenerateRevenueSharingSummaryReportHandler(
        CreateInputAuditCommand createInputAuditCommand,
        CreateOutputAuditCommand createOutputAuditCommand,
        CreateExceptionAuditCommand createExceptionAuditCommand,
        ObjectMapper objectMapper,
        PrincipalCache principalCache,
        ActionAuthorizationManager actionAuthorizationManager,
        ReportDownloadRequestManager reportDownloadRequestManager,
        S3FileStorage s3FileStorage) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.reportDownloadRequestManager = reportDownloadRequestManager;
        this.s3FileStorage = s3FileStorage;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        String normalizedFileType = ReportDownloadUtil.normalizeFileType(input.fileType());
        if (!"xlsx".equals(normalizedFileType) && !"csv".equals(normalizedFileType)) {
            throw new ReportException(ReportErrors.FILE_FORMAT_NOT_ALLOWED_EXCEPTION);
        }

        Map<String, String> params = new HashMap<>();
        params.put("date", this.safe(input.date()));
        params.put("settlementId", this.safe(input.settlementId()));
        params.put("timezoneOffset", this.safe(input.timezoneOffset()));

        ReportDownloadRequestManager.CreateOrReuseResult result =
            this.reportDownloadRequestManager.createPendingOrReuse(
                ReportType.REVENUE_SHARING_SUMMARY, normalizedFileType, params);

        String fileKey = result.request().fileUrl();
        String fileUrl = null;
        if (FileDownloadStatus.READY.equals(result.request().status()) &&
                fileKey != null && !fileKey.isBlank()) {
            try {
                fileUrl = this.s3FileStorage.generatePreSignedDownloadUrl(fileKey);
            } catch (Exception exception) {
                LOG.warn(
                    "Failed to generate pre-signed URL for requestId [{}]: [{}]",
                    result.request().requestId().getEntityId(), exception.getMessage());
            }
        }

        if (FileDownloadStatus.FAILED.equals(result.request().status())) {
            throw new ReportException(
                ReportDownloadUtil.resolveFailedError(
                    result.request().errorMessage(),
                    ReportErrors.REVENUE_SHARING_SUMMARY_REPORT_FAILURE_EXCEPTION));
        }

        return new Output(
            result.request().requestId(), result.request().status(), fileUrl, fileKey,
            result.paramsSignature());
    }

    private String safe(String value) {

        return value == null ? "" : value.trim();
    }
}
