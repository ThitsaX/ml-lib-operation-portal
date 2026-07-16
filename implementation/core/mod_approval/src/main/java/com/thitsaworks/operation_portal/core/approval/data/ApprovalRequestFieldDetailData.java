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

package com.thitsaworks.operation_portal.core.approval.data;

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestFieldDetailId;
import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequestFieldDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApprovalRequestFieldDetailData {

    private ApprovalRequestFieldDetailId approvalRequestFieldDetailId;

    private ApprovalRequestId approvalRequestId;

    private String fieldKey;

    private String fieldLabel;

    private String fieldValue;

    private String beforeValue;

    private String afterValue;

    private String valueType;

    private Integer displayOrder;

    private String tabCode;

    public ApprovalRequestFieldDetailData() { }

    public ApprovalRequestFieldDetailData(ApprovalRequestFieldDetail fieldDetail) {

        this.approvalRequestFieldDetailId = fieldDetail.getApprovalRequestFieldDetailId();
        this.approvalRequestId = fieldDetail.getApprovalRequestId();
        this.fieldKey = fieldDetail.getFieldKey();
        this.fieldLabel = fieldDetail.getFieldLabel();
        this.fieldValue = fieldDetail.getFieldValue();
        this.beforeValue = fieldDetail.getBeforeValue();
        this.afterValue = fieldDetail.getAfterValue();
        this.valueType = fieldDetail.getValueType();
        this.displayOrder = fieldDetail.getDisplayOrder();
        this.tabCode = fieldDetail.getTabCode();
    }

    public String getFieldName() {

        return this.fieldKey;
    }

}
