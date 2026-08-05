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

import com.thitsaworks.operation_portal.component.common.identifier.NdcThresholdStateId;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.core.notification.model.NdcThresholdState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NdcThresholdStateRepository
    extends JpaRepository<NdcThresholdState, NdcThresholdStateId>,
            QuerydslPredicateExecutor<NdcThresholdState> {

    @Query("""
        select s
        from NdcThresholdState s
        where (:participantName is null or s.participantName = :participantName)
          and (:currency is null or s.currency = :currency)
          and (:currentState is null or s.currentState = :currentState)
        order by s.updatedAt desc
        """)
    List<NdcThresholdState> search(@Param("participantName") String participantName,
                                   @Param("currency") String currency,
                                   @Param("currentState") NdcThresholdStateType currentState);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // This will lock the data not to update until this is unlock
    @Query("""
    select s
    from NdcThresholdState s
    where s.participantName = :participantName
      and s.currency = :currency
    """)
    Optional<NdcThresholdState> findByParticipantAndCurrencyForUpdate(
        @Param("participantName") String participantName,
        @Param("currency") String currency);
}
