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
package com.thitsaworks.operation_portal.core.notification.query;

import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdStateType;
import com.thitsaworks.operation_portal.core.notification.data.NdcAlertEventData;
import com.thitsaworks.operation_portal.core.notification.data.NdcNotificationDispatchLogData;
import com.thitsaworks.operation_portal.core.notification.data.NdcThresholdStateData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface NdcRuntimeQuery {

    List<NdcThresholdStateData> getStates(String participantName,
                                          String currency,
                                          NdcThresholdStateType currentState);

    List<NdcAlertEventData> getAlertEvents(String participantName,
                                           String currency,
                                           NdcThresholdStateType currentState,
                                           LocalDateTime from,
                                           LocalDateTime to);

    List<NdcNotificationDispatchLogData> getDispatchLogs(String participantName,
                                                         String currency,
                                                         NdcDeliveryStatus deliveryStatus,
                                                         Instant from,
                                                         Instant to);
}
