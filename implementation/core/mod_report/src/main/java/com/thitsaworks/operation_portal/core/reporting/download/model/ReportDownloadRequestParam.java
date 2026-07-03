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
package com.thitsaworks.operation_portal.core.reporting.download.model;

import com.thitsaworks.operation_portal.component.common.identifier.ReportDownloadRequestId;
import com.thitsaworks.operation_portal.component.common.identifier.ReportDownloadRequestParamId;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_report_request_param")
@Getter
@NoArgsConstructor
public class ReportDownloadRequestParam extends JpaEntity<ReportDownloadRequestParamId> {

    @EmbeddedId
    private ReportDownloadRequestParamId requestParamId;

    @Embedded
    @AttributeOverride(
            name = "id",
            column = @Column(name = "request_id")
    )
    private ReportDownloadRequestId requestId;

    @Column(name = "param_key")
    private String paramKey;

    @Column(name = "param_value")
    private String paramValue;

    public ReportDownloadRequestParam(ReportDownloadRequestId requestId,
                                      String paramKey,
                                      String paramValue,
                                      java.time.Instant createdDate) {

        this.requestParamId = new ReportDownloadRequestParamId(Snowflake.get().nextId());
        this.requestId = requestId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.setCreatedAt(createdDate);
        this.setUpdatedAt(createdDate);
    }

    @Override
    public ReportDownloadRequestParamId getId() {

        return this.requestParamId;
    }
}
