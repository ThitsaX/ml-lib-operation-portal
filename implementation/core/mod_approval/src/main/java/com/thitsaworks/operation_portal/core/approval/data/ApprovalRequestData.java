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

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
public class ApprovalRequestData {

    private ApprovalRequestId approvalRequestId;

    private String fundInOutAction;

    private String participantName;

    private String currency;

    private String participantSettlementCurrencyId;

    private String participantPositionCurrencyId;

    private BigDecimal amount;

    private UserId requestedBy;

    private UserId respondedBy;

    private Instant requestedDtm;

    private ApprovalActionType action;

    private String requestCategory;

    private Instant respondedDtm;

    private List<ApprovalRequestFieldDetailData> fieldDetails = List.of();

    public ApprovalRequestData() { }

    public ApprovalRequestData(ApprovalRequest request) {

        this(request, List.of());
    }

    public ApprovalRequestData(ApprovalRequest request,
                               List<ApprovalRequestFieldDetailData> fieldDetails) {

        this.approvalRequestId = request.getApprovalRequestId();
        this.fundInOutAction = request.getRequestedAction();
        this.participantName = request.getParticipantName();
        this.currency = request.getParticipantCurrency();
        this.participantSettlementCurrencyId = request.getParticipantSettlementCurrencyId();
        this.participantPositionCurrencyId = request.getParticipantPositionCurrencyId();
        this.amount = request.getAmount();
        this.requestedBy = request.getRequestedBy();
        this.respondedBy = request.getRespondedBy();
        this.requestedDtm = request.getRequestedDtm();
        this.action = request.getAction();
        this.requestCategory = request.getRequestCategory();
        this.respondedDtm = request.getRespondedDtm();
        this.fieldDetails = fieldDetails == null ? List.of() : fieldDetails;
    }

}
