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
package com.thitsaworks.operation_portal.core.approval.model.repository;

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

@Repository
public interface ApprovalRequestRepository
    extends JpaRepository<ApprovalRequest, ApprovalRequestId>, QuerydslPredicateExecutor<ApprovalRequest> {

    @Query(
        "SELECT DISTINCT request FROM ApprovalRequest request " +
            "WHERE (:requestedById IS NULL OR request.requestedBy.id = :requestedById) " +
            "AND (" +
            "(:amountTab = TRUE AND (" +
            "EXISTS (" +
            "SELECT detail FROM ApprovalRequestFieldDetail detail " +
            "WHERE detail.approvalRequestId = request.approvalRequestId " +
            "AND (detail.tabCode IS NULL OR detail.tabCode = :tabCode)" +
            ") OR NOT EXISTS (" +
            "SELECT existingDetail FROM ApprovalRequestFieldDetail existingDetail " +
            "WHERE existingDetail.approvalRequestId = request.approvalRequestId" +
            ") " +
            ")) OR " +
            "(:amountTab = FALSE AND EXISTS (" +
            "SELECT detail FROM ApprovalRequestFieldDetail detail " +
            "WHERE detail.approvalRequestId = request.approvalRequestId " +
            "AND detail.tabCode = :tabCode" +
            "))" +
            ")")
    List<ApprovalRequest> findByRequestedByAndTabCode(@Param("requestedById") Long requestedById,
                                                      @Param("tabCode") String tabCode,
                                                      @Param("amountTab") boolean amountTab);

    @Query(
        "SELECT DISTINCT request FROM ApprovalRequest request " +
            "WHERE request.approvalRequestId = :approvalRequestId " +
            "AND request.action = :pendingAction " +
            "AND (" +
            "(:amountTab = TRUE AND (" +
            "EXISTS (" +
            "SELECT detail FROM ApprovalRequestFieldDetail detail " +
            "WHERE detail.approvalRequestId = request.approvalRequestId " +
            "AND (detail.tabCode IS NULL OR detail.tabCode = :tabCode)" +
            ") OR NOT EXISTS (" +
            "SELECT existingDetail FROM ApprovalRequestFieldDetail existingDetail " +
            "WHERE existingDetail.approvalRequestId = request.approvalRequestId" +
            ") " +
            ")) OR " +
            "(:amountTab = FALSE AND EXISTS (" +
            "SELECT detail FROM ApprovalRequestFieldDetail detail " +
            "WHERE detail.approvalRequestId = request.approvalRequestId " +
            "AND detail.tabCode = :tabCode" +
            "))" +
            ")")
    Optional<ApprovalRequest> findPendingByIdAndTabCode(@Param("approvalRequestId") ApprovalRequestId approvalRequestId,
                                                        @Param("pendingAction") ApprovalActionType pendingAction,
                                                        @Param("tabCode") String tabCode,
                                                        @Param("amountTab") boolean amountTab);

    boolean existsByRequestCategoryAndParticipantNameAndParticipantCurrencyAndAction(
        String requestCategory,
        String participantName,
        String participantCurrency,
        ApprovalActionType action);

    List<ApprovalRequest> findAllByRequestCategoryAndActionOrderByRequestedDtmDesc(
        String requestCategory,
        ApprovalActionType action);

    List<ApprovalRequest> findAllByRequestCategoryAndParticipantNameAndActionOrderByRequestedDtmDesc(
        String requestCategory,
        String participantName,
        ApprovalActionType action);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select request
        from ApprovalRequest request
        where request.approvalRequestId = :approvalRequestId
          and request.requestCategory = :requestCategory
          and request.action = :pendingAction
        """)
    Optional<ApprovalRequest> findPendingByIdAndCategoryForUpdate(
        @Param("approvalRequestId") ApprovalRequestId approvalRequestId,
        @Param("requestCategory") String requestCategory,
        @Param("pendingAction") ApprovalActionType pendingAction);
}
