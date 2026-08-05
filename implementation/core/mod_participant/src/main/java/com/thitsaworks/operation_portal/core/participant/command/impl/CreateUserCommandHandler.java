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
import com.thitsaworks.operation_portal.core.participant.command.CreateUserCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.Participant;
import com.thitsaworks.operation_portal.core.participant.model.User;
import com.thitsaworks.operation_portal.core.participant.model.repository.ParticipantRepository;
import com.thitsaworks.operation_portal.core.participant.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateUserCommandHandler implements CreateUserCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserCommandHandler.class);

    private final UserRepository userRepository;

    private final ParticipantRepository participantRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ParticipantException {

        Participant participant = this.participantRepository.findById(input.participantId())
                                                            .orElseThrow(() -> new ParticipantException(
                                                                ParticipantErrors.PARTICIPANT_NOT_FOUND
                                                                    .format(input.participantId().getId().toString())));

        Optional<User> optionalUser =
            this.userRepository.findByEmailAndIsDeleted(input.email(), false);

        if (optionalUser.isPresent()) {
            throw new ParticipantException(ParticipantErrors.EMAIL_ALREADY_REGISTERED.format(input.email()
                                                                                                  .getValue()));

        }

        User user =
            participant.addUser(input.name(), input.email(), input.firstName(), input.lastName(),
                                input.jobTitle(), input.allowNotification(), input.isVisible());

        this.participantRepository.save(participant);

        return new Output(true, user.getUserId());
    }

}
