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
package com.thitsaworks.operation_portal.core.notification.model.repository;

import com.thitsaworks.operation_portal.component.common.identifier.NdcAlertEventId;
import com.thitsaworks.operation_portal.component.common.identifier.NdcNotificationDispatchLogId;
import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.core.notification.model.NdcNotificationDispatchLog;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NdcNotificationDispatchLogRepository
    extends JpaRepository<NdcNotificationDispatchLog, NdcNotificationDispatchLogId>,
            QuerydslPredicateExecutor<NdcNotificationDispatchLog> {

    Optional<NdcNotificationDispatchLog> findByAlertEventIdAndRecipientUserId(NdcAlertEventId alertEventId,
                                                                              String recipientUserId);

    @Query("""
        select d
        from NdcNotificationDispatchLog d
        where d.deliveryStatus in :statuses
          and d.attemptNo < :maximumAttempts
          and (
              d.lastAttemptAt is null
              or d.lastAttemptAt <= :retryBefore
          )
        order by d.createdAt asc
        """)
    List<NdcNotificationDispatchLog> findRetryable(
        @Param("statuses") List<NdcDeliveryStatus> statuses,
        @Param("maximumAttempts") int maximumAttempts,
        @Param("retryBefore") Instant retryBefore,
        Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select d
        from NdcNotificationDispatchLog d
        where d.ndcNotificationDispatchLogId = :id
        """)
    Optional<NdcNotificationDispatchLog> findByIdForUpdate(
        @Param("id") NdcNotificationDispatchLogId id);

    @Query("""
        select d, e.participantName, e.currency
        from NdcNotificationDispatchLog d
        join NdcAlertEvent e on e.ndcAlertEventId = d.alertEventId
        where (:participantName is null or e.participantName = :participantName)
          and (:currency is null or e.currency = :currency)
          and (:deliveryStatus is null or d.deliveryStatus = :deliveryStatus)
          and (:fromTime is null or d.createdAt >= :fromTime)
          and (:toTime is null or d.createdAt <= :toTime)
        order by d.createdAt desc
        """)
    List<Object[]> search(@Param("participantName") String participantName,
                          @Param("currency") String currency,
                          @Param("deliveryStatus") NdcDeliveryStatus deliveryStatus,
                          @Param("fromTime") Instant from,
                          @Param("toTime") Instant to);
}
