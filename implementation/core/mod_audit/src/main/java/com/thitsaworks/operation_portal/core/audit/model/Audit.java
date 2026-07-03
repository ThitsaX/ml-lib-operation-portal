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
package com.thitsaworks.operation_portal.core.audit.model;

import com.thitsaworks.operation_portal.component.common.identifier.ActionId;
import com.thitsaworks.operation_portal.component.common.identifier.AuditId;
import com.thitsaworks.operation_portal.component.common.identifier.RealmId;
import com.thitsaworks.operation_portal.component.common.identifier.TraceId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
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
@Table(name = "tbl_audit")
@Getter
@NoArgsConstructor
public class Audit extends JpaEntity<AuditId> {

    @EmbeddedId
    protected AuditId auditId;

    @Embedded
    protected ActionId actionId;

    @Embedded
    protected UserId userId;

    @Embedded
    protected TraceId traceId;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "participant_id"))
    protected RealmId realmId;

    @Column(name = "input_info")
    protected String inputInfo;

    @Column(name = "output_info")
    protected String outputInfo;

    @Column(name = "exception")
    protected String exception;

    public Audit(ActionId actionId,
                 UserId userId,
                 RealmId realmId,
                 TraceId traceId,
                 String inputInfo,
                 String outputInfo) {

        this.auditId = new AuditId(Snowflake.get()
                                            .nextId());
        this.actionId = actionId;
        this.userId = userId;
        this.realmId = realmId;
        this.traceId = traceId;
        this.inputInfo = inputInfo;
        this.outputInfo = outputInfo;
    }

    @Override
    public AuditId getId() {

        return this.auditId;
    }

    public void outputInfo(String outputInfo) {

        this.outputInfo = outputInfo;
    }

    public void exception(String exception) {

        this.exception = exception;
    }

}
