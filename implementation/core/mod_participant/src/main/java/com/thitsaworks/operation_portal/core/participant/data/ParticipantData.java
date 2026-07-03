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
package com.thitsaworks.operation_portal.core.participant.data;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.Mobile;
import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.common.type.ParticipantStatus;
import com.thitsaworks.operation_portal.core.participant.model.Participant;
import com.thitsaworks.operation_portal.core.participant.model.User;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public record ParticipantData(ParticipantId participantId,
                              String description,
                              Integer dfspId,
                              ParticipantName participantName,
                              String parentParticipantName,
                              String address,
                              Mobile mobile,
                              ParticipantStatus participantStatus,
                              String logoFileType,
                              byte[] logo,
                              Long createdDate,
                              Set<UserId> userIds) implements Serializable {

    public ParticipantData(Participant participant) {

        this(participant.getParticipantId(),
             participant.getDescription(),
             participant.getDfspId(),
             participant.getParticipantName(),
             participant.getParentParticipantName(),
             participant.getAddress(),
             participant.getMobile(),
             participant.getParticipantStatus(),
             participant.getLogoFiletype(),
             participant.getLogoBase64(),
             participant.getCreatedAt()
                        .getEpochSecond(),
             participant.getUsers()
                        .stream()
                        .map((User user) -> new UserId(
                                user.getUserId()
                                    .getId()))
                        .collect(Collectors.toSet()));

    }

}
