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
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.core.notification.model.NdcAlertEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NdcAlertEventRepository
    extends JpaRepository<NdcAlertEvent, NdcAlertEventId>,
            QuerydslPredicateExecutor<NdcAlertEvent> {

    @Query("""
        select e
        from NdcAlertEvent e
        where e.ndcAlertEventId = :alertEventId
        """)
    Optional<NdcAlertEvent> get(@Param("alertEventId") NdcAlertEventId alertEventId);

    Optional<NdcAlertEvent> findByParticipantNDCIdAndBreachCycleNo(ParticipantNDCId participantNDCId,
                                                                   long breachCycleNo);

    @Query("""
        select e
        from NdcAlertEvent e
        where not exists (
            select d.ndcNotificationDispatchLogId
            from NdcNotificationDispatchLog d
            where d.alertEventId = e.ndcAlertEventId
        )
        order by e.eventTime asc
        """)
    List<NdcAlertEvent> findUndispatched(Pageable pageable);

    @Query("""
        select e
        from NdcAlertEvent e
        where (:participantName is null or e.participantName = :participantName)
          and (:currency is null or e.currency = :currency)
          and (:currentState is null or e.currentState = :currentState)
          and (:fromTime is null or e.eventTime >= :fromTime)
          and (:toTime is null or e.eventTime <= :toTime)
        order by e.eventTime desc
        """)
    List<NdcAlertEvent> search(@Param("participantName") String participantName,
                               @Param("currency") String currency,
                               @Param("currentState") NdcThresholdStateType currentState,
                               @Param("fromTime") LocalDateTime from,
                               @Param("toTime") LocalDateTime to);
}
