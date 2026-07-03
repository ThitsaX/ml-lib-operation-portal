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

import com.thitsaworks.operation_portal.component.common.identifier.ContactId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.type.ContactType;
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.component.common.type.Mobile;
import com.thitsaworks.operation_portal.core.participant.model.Contact;

public record ContactData(ContactId contactId,

                          ParticipantId participantId,

                          String name,

                          String position,

                          Email email,

                          Mobile mobile,

                          ContactType contactType) {

    public ContactData(Contact contact) {

        this(contact.getContactId(),

             contact.getParticipant().getParticipantId(),

             contact.getName(),

             contact.getPosition(),

             contact.getEmail(),

             contact.getMobile(),

             contact.getContactType());

    }

}
