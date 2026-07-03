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
package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.type.ContactType;
import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.common.type.ParticipantStatus;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.component.common.type.Mobile;

import java.io.Serializable;
import java.util.List;

public interface CreateParticipant
    extends UseCase<CreateParticipant.Input, CreateParticipant.Output> {

    record Input(int participantId,
                 ParticipantName participantName,
                 String description,
                 String address,
                 Mobile mobile,
                 ParticipantStatus status,
                 List<ContactInfo> contactInfoList,
                 List<LiquidityProfileInfo> liquidityProfileInfoList
    ) implements Serializable {

        public record ContactInfo(String name,
                                  String position,
                                  Email email,
                                  Mobile mobile,
                                  ContactType contactType
        ) implements Serializable { }

        public record LiquidityProfileInfo(String accountName,
                                           String accountNumber,
                                           String currency,
                                           Boolean status
        ) implements Serializable { }

    }

    record Output(boolean created,
                  ParticipantId participantId
    ) implements Serializable { }

}
