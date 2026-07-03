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
import com.thitsaworks.operation_portal.component.common.identifier.GreetingId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.participant.data.GreetingData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.QGreetingMessage;
import com.thitsaworks.operation_portal.core.participant.model.repository.GreetingRepository;
import com.thitsaworks.operation_portal.core.participant.query.GreetingQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class GreetingQueryHandler implements GreetingQuery {

    private final GreetingRepository greetingRepository;

    private final QGreetingMessage greeting = QGreetingMessage.greetingMessage;

    @Override
    public List<GreetingData> getGreeting() {

        return this.greetingRepository.findAll()
                                      .stream()
                                      .map(Greeting -> new GreetingData(Greeting.getId()
                                              , Greeting.getGreetingTitle(),
                                              Greeting.getGreetingDetail(),
                                              Greeting.isDeleted(),
                                              Greeting.getGreetingDate()
                                      ))
                                      .collect(Collectors.toList());
    }

    @Override
    public Optional<GreetingData> getLatestGreeting() {

        return this.greetingRepository.findAll()
                                      .stream()
                                      .filter(greeting -> !greeting.isDeleted())
                                      .sorted((g1, g2) -> g2.getGreetingDate().compareTo(g1.getGreetingDate()))
                                      .findFirst()
                                      .map(greeting -> new GreetingData(
                                              greeting.getId(),
                                              greeting.getGreetingTitle(),
                                              greeting.getGreetingDetail(),
                                              greeting.isDeleted(),
                                              greeting.getGreetingDate()
                                      ));
    }

    @Override
    public GreetingData get(GreetingId greetingId) throws ParticipantException {

        BooleanExpression predicate = this.greeting.greetingId.eq(greetingId);

        return this.greetingRepository.findOne(predicate)
                                      .map(Greeting -> new GreetingData(Greeting.getId(),
                                              Greeting.getGreetingTitle(),
                                              Greeting.getGreetingDetail(),
                                              Greeting.isDeleted(),
                                              Greeting.getGreetingDate()
                                      ))
                                      .orElseThrow(
                                              () -> new ParticipantException(ParticipantErrors.GREETING_MESSAGE_NOT_FOUND.format(greetingId.getId().toString())));


    }

}