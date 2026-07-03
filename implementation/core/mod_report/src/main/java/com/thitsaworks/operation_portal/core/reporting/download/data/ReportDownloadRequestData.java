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
package com.thitsaworks.operation_portal.core.reporting.download.data;

import com.thitsaworks.operation_portal.component.common.identifier.ReportDownloadRequestId;
import com.thitsaworks.operation_portal.component.common.type.FileDownloadStatus;
import com.thitsaworks.operation_portal.component.common.type.ReportType;
import com.thitsaworks.operation_portal.core.reporting.download.model.ReportDownloadRequest;

import java.time.Instant;

public record ReportDownloadRequestData(ReportDownloadRequestId requestId,
                                        ReportType reportType,
                                        String paramsSignature,
                                        FileDownloadStatus status,
                                        String fileType,
                                        String fileUrl,
                                        String errorMessage,
                                        Instant createdAt,
                                        Instant updatedAt,
                                        Instant finishedDate) {

    public ReportDownloadRequestData(ReportDownloadRequest request) {

        this(request.getRequestId(),
             request.getReportType(),
             request.getParamsSignature(),
             request.getStatus(),
             request.getFileType(),
             request.getFileUrl(),
             request.getErrorMessage(),
             request.getCreatedAt(),
             request.getUpdatedAt(),
             request.getFinishedDate());
    }
}
