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

package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface GetPendingApprovalList
    extends UseCase<GetPendingApprovalList.Input, GetPendingApprovalList.Output> {

    record Input(ApprovalTabCode tabCode) { }

    record Output(List<PendingApproval> pendingApprovalList) {

        public record PendingApproval(ApprovalRequestId approvalRequestId,
                                      String requestedAction,
                                      String participantName,
                                      String currency,
                                      BigDecimal amount,
                                      String requestedBy,
                                      Instant requestedDateTime,
                                      String respondedBy,
                                      Instant respondedDateTime,
                                      ApprovalActionType action,
                                      String requestCategory,
                                      List<PendingApprovalDetail> details) { }

        public record PendingApprovalDetail(String tabCode,
                                            String fieldKey,
                                            String fieldLabel,
                                            String fieldValue,
                                            String beforeValue,
                                            String afterValue,
                                            String valueType,
                                            Integer displayOrder) { }

    }

}
