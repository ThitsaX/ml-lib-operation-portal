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
package com.thitsaworks.operation_portal.core.audit.data;

import com.thitsaworks.operation_portal.component.common.identifier.ActionId;
import com.thitsaworks.operation_portal.component.common.identifier.AuditId;
import com.thitsaworks.operation_portal.component.common.identifier.RealmId;
import com.thitsaworks.operation_portal.component.common.identifier.TraceId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.core.audit.model.Audit;

public record AuditData(AuditId auditId,
                        ActionId actionId,
                        UserId userId,
                        RealmId realmId,
                        TraceId traceId,
                        String inputInfo,
                        String outputInfo,
                        String exceptionInfo) {

    public AuditData(Audit audit) {

        this(audit.getAuditId(),
             audit.getActionId(),
             audit.getUserId(),
             audit.getRealmId(),
             audit.getTraceId(),
             audit.getInputInfo(),
             audit.getOutputInfo(),
             audit.getException());
    }

}
