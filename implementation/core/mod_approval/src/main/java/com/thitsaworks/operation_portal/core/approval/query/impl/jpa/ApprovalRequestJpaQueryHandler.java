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
package com.thitsaworks.operation_portal.core.approval.query.impl.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestData;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalErrors;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequest;
import com.thitsaworks.operation_portal.core.approval.model.QApprovalRequest;
import com.thitsaworks.operation_portal.core.approval.model.repository.ApprovalRequestRepository;
import com.thitsaworks.operation_portal.core.approval.query.ApprovalRequestQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class ApprovalRequestJpaQueryHandler implements ApprovalRequestQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalRequestJpaQueryHandler.class);

    private final QApprovalRequest approvalRequest = QApprovalRequest.approvalRequest;

    private final ApprovalRequestRepository approvalRequestRepository;

    @Override
    public List<ApprovalRequestData> getPendingApprovalRequests() {

        BooleanExpression predicate = this.approvalRequest.isNotNull();

        var pendingApprovalRequest = (List<ApprovalRequest>) this.approvalRequestRepository.findAll(predicate);

        return pendingApprovalRequest.stream()
                                     .map(ApprovalRequestData::new)
                                     .toList();
    }

    @Override
    public List<ApprovalRequestData> getPendingApprovalRequestsByRequestedId(UserId userId) {

        BooleanExpression predicate = this.approvalRequest.requestedBy.eq(userId);

        var pendingApprovalRequest = (List<ApprovalRequest>) this.approvalRequestRepository.findAll(predicate);

        return pendingApprovalRequest.stream()
                                     .map(ApprovalRequestData::new)
                                     .toList();
    }

    @Override
    public ApprovalRequestData getPendingApprovalRequestByID(ApprovalRequestId approvalRequestId)
        throws ApprovalException {

        if (approvalRequestId == null) {
            throw new ApprovalException(ApprovalErrors.INVALID_APPROVAL_REQUEST);
        }

        BooleanExpression predicate = this.approvalRequest.approvalRequestId.eq(approvalRequestId)
                                                                            .and(this.approvalRequest.action.eq(
                                                                                ApprovalActionType.PENDING));

        return this.approvalRequestRepository.findOne(predicate)
                                             .map(ApprovalRequestData::new)
                                             .orElseThrow(() -> new ApprovalException(ApprovalErrors.APPROVAL_REQUEST_NOT_FOUND.format(
                                                 approvalRequestId.getId()
                                                                  .toString())));
    }

}
