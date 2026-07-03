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
import com.thitsaworks.operation_portal.core.participant.command.UpdateGreetingCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.GreetingMessage;
import com.thitsaworks.operation_portal.core.participant.model.repository.GreetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateHomeMessageCommandHandler implements UpdateGreetingCommand {

    private final GreetingRepository greetingRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ParticipantException {

        var greeting = this.greetingRepository.findById(input.greetingId())
                                              .orElseThrow(() -> new ParticipantException(
                                                      ParticipantErrors.GREETING_MESSAGE_NOT_FOUND.format(input.greetingId().getId().toString())));

        Optional<GreetingMessage> optionalGreetingMessage = this.greetingRepository.findOne(
            GreetingRepository.Filters.findByGreetingTitle(input.greetingTitle()));

        if (optionalGreetingMessage.isPresent() &&
                !optionalGreetingMessage.get().getGreetingId().equals(greeting.getGreetingId())) {
            throw new ParticipantException(ParticipantErrors.GREETING_MESSAGE_ALREADY_REGISTERED.format(input.greetingTitle()));
        }

        greeting.greetingTitle(input.greetingTitle());
        greeting.greetingDetail(input.greetingDetail());
        greeting.isDeleted(input.isDeleted());
        greeting.greetingDate(input.greetDate());



        this.greetingRepository.save(greeting);

        return new Output(greeting.getGreetingId());

    }


    }

