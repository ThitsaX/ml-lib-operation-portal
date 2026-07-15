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
package com.thitsaworks.operation_portal.core.notification.query.impl.jpa;

import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.notification.data.NdcAlertEventData;
import com.thitsaworks.operation_portal.core.notification.data.NdcNotificationDispatchLogData;
import com.thitsaworks.operation_portal.core.notification.data.NdcThresholdStateData;
import com.thitsaworks.operation_portal.core.notification.model.NdcAlertEvent;
import com.thitsaworks.operation_portal.core.notification.model.NdcNotificationDispatchLog;
import com.thitsaworks.operation_portal.core.notification.model.NdcThresholdState;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcAlertEventRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcNotificationDispatchLogRepository;
import com.thitsaworks.operation_portal.core.notification.model.repository.NdcThresholdStateRepository;
import com.thitsaworks.operation_portal.core.notification.query.NdcRuntimeQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class NdcRuntimeJpaQueryHandler implements NdcRuntimeQuery {

    private final NdcThresholdStateRepository ndcThresholdStateRepository;

    private final NdcAlertEventRepository ndcAlertEventRepository;

    private final NdcNotificationDispatchLogRepository ndcNotificationDispatchLogRepository;

    @Override
    public List<NdcThresholdStateData> getStates(String participantName,
                                                 String currency,
                                                 NdcThresholdStateType currentState) {

        return this.ndcThresholdStateRepository.search(participantName, currency, currentState)
                                              .stream()
                                              .map(row -> {
                                                  NdcThresholdState state = (NdcThresholdState) row[0];
                                                  return new NdcThresholdStateData(
                                                      state.getNdcThresholdStateId(),
                                                      state.getParticipantNDCId(),
                                                      (String) row[1],
                                                      (String) row[2],
                                                      state.getCurrentState(),
                                                      state.getBreachCycleNo(),
                                                      state.getLastEvaluatedBalance(),
                                                      state.getLastEvaluatedNdcUsed(),
                                                      state.getLastBreachedAt(),
                                                      state.getLastRecoveredAt(),
                                                      state.getCreatedAt(),
                                                      state.getUpdatedAt());
                                              })
                                              .toList();
    }

    @Override
    public List<NdcAlertEventData> getAlertEvents(String participantName,
                                                  String currency,
                                                  NdcThresholdStateType currentState,
                                                  LocalDateTime from,
                                                  LocalDateTime to) {

        return this.ndcAlertEventRepository.search(participantName, currency, currentState, from, to)
                                           .stream()
                                           .map(this::toAlertEventData)
                                           .toList();
    }

    @Override
    public List<NdcNotificationDispatchLogData> getDispatchLogs(String participantName,
                                                                String currency,
                                                                NdcDeliveryStatus deliveryStatus,
                                                                LocalDateTime from,
                                                                LocalDateTime to) {

        return this.ndcNotificationDispatchLogRepository.search(participantName, currency, deliveryStatus, from, to)
                                                        .stream()
                                                        .map(row -> {
                                                            NdcNotificationDispatchLog log =
                                                                (NdcNotificationDispatchLog) row[0];
                                                            return new NdcNotificationDispatchLogData(
                                                                log.getNdcNotificationDispatchLogId(),
                                                                log.getAlertEventId(),
                                                                log.getParticipantNDCId(),
                                                                (String) row[1],
                                                                (String) row[2],
                                                                log.getRecipientType(),
                                                                log.getRecipientUserId(),
                                                                log.getRecipientName(),
                                                                log.getRecipientEmail(),
                                                                log.getDeliveryStatus(),
                                                                log.getAttemptNo(),
                                                                log.getLastAttemptAt(),
                                                                log.getSentAt(),
                                                                log.getErrorMessage(),
                                                                log.getCreatedAt(),
                                                                log.getUpdatedAt());
                                                        })
                                                        .toList();
    }

    private NdcAlertEventData toAlertEventData(NdcAlertEvent event) {

        return new NdcAlertEventData(
            event.getNdcAlertEventId(),
            event.getParticipantNDCId(),
            event.getParticipantName(),
            event.getCurrency(),
            event.getBreachCycleNo(),
            event.getPreviousState(),
            event.getCurrentState(),
            event.getThresholdPercent(),
            event.getCurrentBalance(),
            event.getCurrentNdcUsed(),
            event.getEventMessage(),
            event.getEventTime(),
            event.getCreatedAt());
    }
}
