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
package com.thitsaworks.operation_portal.core.audit.query;

import com.thitsaworks.operation_portal.component.common.identifier.ActionId;
import com.thitsaworks.operation_portal.component.common.identifier.AuditId;
import com.thitsaworks.operation_portal.component.common.identifier.RealmId;
import com.thitsaworks.operation_portal.component.common.identifier.TraceId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.Email;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public interface GetAllAuditByParticipantQuery {

    Output execute(Input input);

    record Input(RealmId realmId,
                 Instant fromDate,
                 Instant toDate,
                 List<ActionId> grantedActionList,
                 UserId userId,
                 ActionId actionId,
                 int page,
                 int size
    ) { }

    record Output(List<AuditInfo> auditInfoList,
                  long totalElements,
                  int totalPages
    ) {

        public record AuditInfo(AuditId auditId,
                                Instant date,
                                String action,
                                Email madeBy,
                                TraceId traceId) implements Serializable { }

    }

}
