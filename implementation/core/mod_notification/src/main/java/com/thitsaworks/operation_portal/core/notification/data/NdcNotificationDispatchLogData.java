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
package com.thitsaworks.operation_portal.core.notification.data;

import com.thitsaworks.operation_portal.component.common.identifier.NdcAlertEventId;
import com.thitsaworks.operation_portal.component.common.identifier.NdcNotificationDispatchLogId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantNDCId;
import com.thitsaworks.operation_portal.component.common.type.NdcDeliveryStatus;
import com.thitsaworks.operation_portal.component.common.type.NdcRecipientType;

import java.time.LocalDateTime;

public record NdcNotificationDispatchLogData(NdcNotificationDispatchLogId ndcNotificationDispatchLogId,
                                             NdcAlertEventId alertEventId,
                                             ParticipantNDCId participantNDCId,
                                             String participantName,
                                             String currency,
                                             NdcRecipientType recipientType,
                                             String recipientUserId,
                                             String recipientName,
                                             String recipientEmail,
                                             NdcDeliveryStatus deliveryStatus,
                                             int attemptNo,
                                             LocalDateTime lastAttemptAt,
                                             LocalDateTime sentAt,
                                             String errorMessage,
                                             LocalDateTime createdAt,
                                             LocalDateTime updatedAt) {
}
