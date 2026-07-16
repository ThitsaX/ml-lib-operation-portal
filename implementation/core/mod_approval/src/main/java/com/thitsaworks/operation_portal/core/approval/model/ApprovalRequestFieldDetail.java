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

package com.thitsaworks.operation_portal.core.approval.model;

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestFieldDetailId;
import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_approval_request_field_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApprovalRequestFieldDetail extends JpaEntity<ApprovalRequestFieldDetailId> {

    @EmbeddedId
    protected ApprovalRequestFieldDetailId approvalRequestFieldDetailId;

    @Embedded
    @AttributeOverride(
        name = "id",
        column = @Column(name = "approval_request_id"))
    protected ApprovalRequestId approvalRequestId;

    @Column(name = "field_key")
    protected String fieldKey;

    @Column(name = "field_label")
    protected String fieldLabel;

    @Column(name = "field_value", columnDefinition = "MEDIUMTEXT")
    protected String fieldValue;

    @Column(name = "before_value", columnDefinition = "MEDIUMTEXT")
    protected String beforeValue;

    @Column(name = "after_value", columnDefinition = "MEDIUMTEXT")
    protected String afterValue;

    @Column(name = "value_type")
    protected String valueType;

    @Column(name = "display_order")
    protected Integer displayOrder;

    @Column(name = "tab_code")
    protected String tabCode;

    public ApprovalRequestFieldDetail(ApprovalRequestId approvalRequestId,
                                      String fieldKey,
                                      String fieldLabel,
                                      String fieldValue,
                                      String beforeValue,
                                      String afterValue,
                                      String valueType,
                                      Integer displayOrder,
                                      String tabCode) {

        this.approvalRequestFieldDetailId = new ApprovalRequestFieldDetailId(
            Snowflake.get().nextId());
        this.approvalRequestId = approvalRequestId;
        this.fieldKey = fieldKey;
        this.fieldLabel = fieldLabel;
        this.fieldValue = fieldValue;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.valueType = valueType;
        this.displayOrder = displayOrder;
        this.tabCode = tabCode;
    }

    @Override
    public ApprovalRequestFieldDetailId getId() {

        return this.approvalRequestFieldDetailId;
    }

    public String getFieldName() {

        return this.fieldKey;
    }

}
