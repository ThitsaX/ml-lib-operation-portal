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

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestFieldDetailId;
import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequestFieldDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestFieldDetailRepository
    extends JpaRepository<ApprovalRequestFieldDetail, ApprovalRequestFieldDetailId>,
            QuerydslPredicateExecutor<ApprovalRequestFieldDetail> {

    List<ApprovalRequestFieldDetail> findByApprovalRequestIdOrderByDisplayOrderAsc(ApprovalRequestId approvalRequestId);

    @Query(
        "SELECT detail FROM ApprovalRequestFieldDetail detail " +
            "WHERE detail.approvalRequestId = :approvalRequestId " +
            "AND (" +
            "(:amountTab = TRUE AND (detail.tabCode IS NULL OR detail.tabCode = :tabCode)) " +
            "OR (:amountTab = FALSE AND detail.tabCode = :tabCode)" +
            ") " +
            "ORDER BY detail.displayOrder ASC")
    List<ApprovalRequestFieldDetail> findByApprovalRequestIdAndTabCodeOrderByDisplayOrderAsc(
        @Param("approvalRequestId") ApprovalRequestId approvalRequestId,
        @Param("tabCode") String tabCode,
        @Param("amountTab") boolean amountTab);
}
