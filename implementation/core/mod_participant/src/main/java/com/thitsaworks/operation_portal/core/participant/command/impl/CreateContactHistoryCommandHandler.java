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
package com.thitsaworks.operation_portal.core.participant.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.participant.command.CreateContactHistoryCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.ContactHistory;
import com.thitsaworks.operation_portal.core.participant.model.repository.ContactHistoryRepository;
import com.thitsaworks.operation_portal.core.participant.model.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateContactHistoryCommandHandler implements CreateContactHistoryCommand {

    private final ContactHistoryRepository contactHistoryRepository;

    private final ContactRepository contactRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ParticipantException {

        var contact= this.contactRepository.findById(input.contactId())
                                                  .orElseThrow(()-> new ParticipantException(ParticipantErrors.CONTACT_NOT_FOUND.format(input.contactId().getId().toString())));

        var history =new ContactHistory(contact.getContactId(),
                                        input.participantId(),
                                        contact.getName(),
                                        contact.getPosition(),
                                        contact.getEmail(),
                                        contact.getMobile(),
                                        contact.getContactType());

        this.contactHistoryRepository.save(history);

        return new Output(history.getContactHistoryId());
    }

}
