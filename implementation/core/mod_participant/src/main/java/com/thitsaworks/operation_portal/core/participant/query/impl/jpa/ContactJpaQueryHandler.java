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
package com.thitsaworks.operation_portal.core.participant.query.impl.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.ContactId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.participant.data.ContactData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.Contact;
import com.thitsaworks.operation_portal.core.participant.model.QContact;
import com.thitsaworks.operation_portal.core.participant.model.repository.ContactRepository;
import com.thitsaworks.operation_portal.core.participant.query.ContactQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class ContactJpaQueryHandler implements ContactQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactJpaQueryHandler.class);

    private final ContactRepository contactRepository;

    private final QContact contact = QContact.contact;

    @Override
    public List<ContactData> getContacts(ParticipantId participantId) {

        BooleanExpression predicate = this.contact.participant.participantId.eq(participantId);

        List<Contact> contacts = (List<Contact>) this.contactRepository.findAll(predicate);

        return contacts.stream()
                       .map(ContactData::new)
                       .toList();

    }

    @Override
    public ContactData get(ContactId contactId) throws ParticipantException {

        BooleanExpression predicate = this.contact.contactId.eq(contactId);

        Optional<Contact> optionalContact = this.contactRepository.findOne(predicate);

        if (optionalContact.isEmpty()) {

            throw new ParticipantException(ParticipantErrors.CONTACT_NOT_FOUND.format(contactId.getId().toString()));
        }

        return new ContactData(optionalContact.get());
    }

}
