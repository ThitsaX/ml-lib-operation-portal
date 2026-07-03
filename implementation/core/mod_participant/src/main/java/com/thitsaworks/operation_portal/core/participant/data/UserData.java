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
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.core.participant.model.User;

import java.io.Serializable;

public record UserData(UserId userId,
                       ParticipantId participantId,
                       ParticipantName participantName,
                       String participantDescription,
                       String name,
                       Email email,
                       String firstName,
                       String lastName,
                       String jobTitle,
                       boolean isDeleted,
                       Long createdDate) implements Serializable {

    public UserData(User user) {

        this(user.getUserId(),
             user.getParticipant().getParticipantId(),
             user.getParticipant().getParticipantName(),
             user.getParticipant().getDescription(),
             user.getName(),
             user.getEmail(),
             user.getFirstName(),
             user.getLastName(),
             user.getJobTitle(),
             user.isDeleted(),
             user.getCreatedAt().getEpochSecond());
    }

}
