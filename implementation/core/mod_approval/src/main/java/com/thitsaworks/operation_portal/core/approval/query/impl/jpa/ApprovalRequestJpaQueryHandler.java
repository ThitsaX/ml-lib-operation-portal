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
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestFieldDetailData;
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestData;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalErrors;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequest;
import com.thitsaworks.operation_portal.core.approval.model.QApprovalRequest;
import com.thitsaworks.operation_portal.core.approval.model.repository.ApprovalRequestFieldDetailRepository;
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

    private static final String AMOUNT_TAB_CODE = "AMOUNT";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalRequestJpaQueryHandler.class);

    private final QApprovalRequest approvalRequest = QApprovalRequest.approvalRequest;

    private final ApprovalRequestRepository approvalRequestRepository;

    private final ApprovalRequestFieldDetailRepository approvalRequestFieldDetailRepository;

    @Override
    public List<ApprovalRequestData> getPendingApprovalRequests() {

        BooleanExpression predicate = this.approvalRequest.isNotNull();

        var pendingApprovalRequest = (List<ApprovalRequest>) this.approvalRequestRepository.findAll(predicate);

        return pendingApprovalRequest.stream()
                                     .map(this::toData)
                                     .toList();
    }

    @Override
    public List<ApprovalRequestData> getPendingApprovalRequestsByTabCode(String tabCode) {

        var pendingApprovalRequest = this.approvalRequestRepository.findByRequestedByAndTabCode(
            null, this.normalizeTabCode(tabCode), this.isAmountTabCode(tabCode));

        return pendingApprovalRequest.stream()
                                     .map(request -> this.toData(request, tabCode))
                                     .toList();
    }

    @Override
    public List<ApprovalRequestData> getPendingApprovalRequestsByRequestedId(UserId userId) {

        BooleanExpression predicate = this.approvalRequest.requestedBy.eq(userId);

        var pendingApprovalRequest = (List<ApprovalRequest>) this.approvalRequestRepository.findAll(predicate);

        return pendingApprovalRequest.stream()
                                     .map(this::toData)
                                     .toList();
    }

    @Override
    public List<ApprovalRequestData> getPendingApprovalRequestsByRequestedIdAndTabCode(UserId userId,
                                                                                      String tabCode) {

        var pendingApprovalRequest = this.approvalRequestRepository.findByRequestedByAndTabCode(
            userId.getId(), this.normalizeTabCode(tabCode), this.isAmountTabCode(tabCode));

        return pendingApprovalRequest.stream()
                                     .map(request -> this.toData(request, tabCode))
                                     .toList();
    }

    @Override
    public ApprovalRequestData getPendingApprovalRequestByID(ApprovalRequestId approvalRequestId,
                                                             String tabCode)
        throws ApprovalException {

        if (approvalRequestId == null) {
            throw new ApprovalException(ApprovalErrors.INVALID_APPROVAL_REQUEST);
        }

        var normalizedTabCode = this.normalizeTabCode(tabCode);

        return this.approvalRequestRepository.findPendingByIdAndTabCode(
                                                 approvalRequestId, ApprovalActionType.PENDING, normalizedTabCode,
                                                 this.isAmountTabCode(normalizedTabCode))
                                             .map(request -> this.toData(request, normalizedTabCode))
                                             .orElseThrow(() -> new ApprovalException(ApprovalErrors.APPROVAL_REQUEST_NOT_FOUND.format(
                                                 approvalRequestId.getId()
                                                                  .toString())));
    }

    private ApprovalRequestData toData(ApprovalRequest request) {

        var fieldDetails = this.approvalRequestFieldDetailRepository
            .findByApprovalRequestIdOrderByDisplayOrderAsc(request.getApprovalRequestId())
            .stream()
            .map(ApprovalRequestFieldDetailData::new)
            .toList();

        return new ApprovalRequestData(request, fieldDetails);
    }

    private ApprovalRequestData toData(ApprovalRequest request, String tabCode) {

        var normalizedTabCode = this.normalizeTabCode(tabCode);
        var fieldDetails = this.approvalRequestFieldDetailRepository
            .findByApprovalRequestIdAndTabCodeOrderByDisplayOrderAsc(
                request.getApprovalRequestId(), normalizedTabCode, this.isAmountTabCode(normalizedTabCode))
            .stream()
            .map(ApprovalRequestFieldDetailData::new)
            .toList();

        return new ApprovalRequestData(request, fieldDetails);
    }

    private String normalizeTabCode(String tabCode) {

        return tabCode == null ? null : tabCode.trim()
                                                .toUpperCase();
    }

    private boolean isAmountTabCode(String tabCode) {

        return AMOUNT_TAB_CODE.equalsIgnoreCase(tabCode);
    }

}
