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
package com.thitsaworks.operation_portal.core.approval.command;

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestFieldDetailId;
import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;

public interface CreateApprovalRequestFieldDetailCommand {

    Output execute(Input input);

    record Input(ApprovalRequestId approvalRequestId,
                 String fieldKey,
                 String fieldLabel,
                 String fieldValue,
                 String beforeValue,
                 String afterValue,
                 String valueType,
                 Integer displayOrder,
                 String tabCode) { }

    record Output(ApprovalRequestFieldDetailId approvalRequestFieldDetailId) { }

}
