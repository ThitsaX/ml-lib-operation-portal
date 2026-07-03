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
package com.thitsaworks.operation_portal.core.reporting.download.request;

import com.thitsaworks.operation_portal.component.common.type.FileDownloadStatus;
import com.thitsaworks.operation_portal.component.common.type.ReportType;
import com.thitsaworks.operation_portal.component.misc.persistence.PersistenceQualifiers;
import com.thitsaworks.operation_portal.core.reporting.download.data.ReportDownloadRequestData;
import com.thitsaworks.operation_portal.core.reporting.download.model.ReportDownloadRequest;
import com.thitsaworks.operation_portal.core.reporting.download.model.ReportDownloadRequestParam;
import com.thitsaworks.operation_portal.core.reporting.download.model.repository.ReportDownloadRequestParamRepository;
import com.thitsaworks.operation_portal.core.reporting.download.model.repository.ReportDownloadRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportDownloadRequestManager {

    private final ReportDownloadRequestRepository reportDownloadRequestRepository;

    private final ReportDownloadRequestParamRepository reportDownloadRequestParamRepository;

    public ReportDownloadRequestManager(ReportDownloadRequestRepository reportDownloadRequestRepository,
                                        ReportDownloadRequestParamRepository reportDownloadRequestParamRepository) {

        this.reportDownloadRequestRepository = reportDownloadRequestRepository;
        this.reportDownloadRequestParamRepository = reportDownloadRequestParamRepository;
    }


    @Transactional(transactionManager = PersistenceQualifiers.Core.TRANSACTION_MANAGER)
    public CreateOrReuseResult createPendingOrReuse(ReportType reportType,
                                                    String fileType,
                                                    Map<String, String> params) {

        String normalizedFileType = normalizeFileType(fileType);
        String signature = ReportRequestSignature.from(
            reportType.name(), normalizedFileType, params);

        Instant now = Instant.now();
        ReportDownloadRequest request = new ReportDownloadRequest(
            reportType, signature, FileDownloadStatus.PENDING,
            normalizedFileType, now, now);

        this.reportDownloadRequestRepository.save(request);

        params.forEach((key, value) -> this.reportDownloadRequestParamRepository.save(
            new ReportDownloadRequestParam(request.getId(), key, value, now)));

        return new CreateOrReuseResult(
            new ReportDownloadRequestData(request), signature);
    }

    private String normalizeFileType(String fileType) {

        return fileType == null ? "" : fileType.trim().toLowerCase(Locale.ROOT);
    }

    public record CreateOrReuseResult(ReportDownloadRequestData request,
                                      String paramsSignature) {

    }

}
