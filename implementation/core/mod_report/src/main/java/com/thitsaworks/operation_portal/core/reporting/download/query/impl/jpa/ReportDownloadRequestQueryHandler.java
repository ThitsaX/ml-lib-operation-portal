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
package com.thitsaworks.operation_portal.core.reporting.download.query.impl.jpa;

import com.thitsaworks.operation_portal.component.common.identifier.ReportDownloadRequestId;
import com.thitsaworks.operation_portal.component.common.type.FileDownloadStatus;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.reporting.download.data.ReportDownloadRequestData;
import com.thitsaworks.operation_portal.core.reporting.download.exception.ReportDownloadErrors;
import com.thitsaworks.operation_portal.core.reporting.download.model.repository.ReportDownloadRequestRepository;
import com.thitsaworks.operation_portal.core.reporting.download.query.ReportDownloadRequestQuery;
import com.thitsaworks.operation_portal.reporting.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class ReportDownloadRequestQueryHandler implements ReportDownloadRequestQuery {

    private final ReportDownloadRequestRepository reportDownloadRequestRepository;

    @Override
    public FileDownloadStatus getStatus(ReportDownloadRequestId id) throws ReportException {

        var reportDownloadRequest = this.reportDownloadRequestRepository
                                        .findById(id)
                                        .orElseThrow(() -> new ReportException(
                                            ReportDownloadErrors.REQUEST_NOT_FOUND));

        return reportDownloadRequest.getStatus();
    }

    @Override
    public Optional<ReportDownloadRequestData> findById(ReportDownloadRequestId id) {

        return this.reportDownloadRequestRepository.findById(id)
                                                   .map(ReportDownloadRequestData::new);
    }

    @Override
    public Optional<ReportDownloadRequestData> findTopByStatusOrderByCreatedAtAsc(FileDownloadStatus status) {

        var optReportDownloadRequest = this.reportDownloadRequestRepository.findTopByStatusOrderByCreatedAtAsc(
            status);

        return optReportDownloadRequest.map(ReportDownloadRequestData::new);
    }

}
